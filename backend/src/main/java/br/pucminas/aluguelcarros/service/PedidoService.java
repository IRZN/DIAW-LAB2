package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.PedidoDTO.*;
import br.pucminas.aluguelcarros.exception.AcessoNegadoException;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.*;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import br.pucminas.aluguelcarros.model.enums.TipoProprietario;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import br.pucminas.aluguelcarros.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Subsistema de gestão de pedidos e contratos.
 * Clientes: introduzem, modificam, consultam e cancelam pedidos.
 * Agentes: modificam e avaliam pedidos (análise financeira) e executam contratos.
 */
@Service
public class PedidoService {

    private final PedidoAluguelRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final AgenteRepository agenteRepository;
    private final ContratoRepository contratoRepository;
    private final AutomovelService automovelService;

    public PedidoService(PedidoAluguelRepository pedidoRepository, ClienteRepository clienteRepository,
                         AgenteRepository agenteRepository, ContratoRepository contratoRepository,
                         AutomovelService automovelService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.agenteRepository = agenteRepository;
        this.contratoRepository = contratoRepository;
        this.automovelService = automovelService;
    }

    // ---------- Consulta ----------

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioLogado usuario, StatusPedido status) {
        List<PedidoAluguel> pedidos;
        if (usuario.isCliente()) {
            pedidos = pedidoRepository.findByClienteIdOrderByCriadoEmDesc(usuario.id());
            if (status != null) {
                pedidos = pedidos.stream().filter(p -> p.getStatus() == status).toList();
            }
        } else {
            pedidos = status == null
                    ? pedidoRepository.findAllByOrderByCriadoEmDesc()
                    : pedidoRepository.findByStatusOrderByCriadoEmDesc(status);
        }
        return pedidos.stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, UsuarioLogado usuario) {
        return PedidoResponse.de(obterComAcesso(id, usuario));
    }

    // ---------- Cliente ----------

    @Transactional
    public PedidoResponse criar(PedidoRequest req, UsuarioLogado usuario) {
        if (!usuario.isCliente()) {
            throw new AcessoNegadoException("Somente clientes podem introduzir pedidos de aluguel.");
        }
        Cliente cliente = clienteRepository.findById(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", usuario.id()));

        PedidoAluguel pedido = new PedidoAluguel();
        pedido.setCliente(cliente);
        aplicarDados(pedido, req, true);
        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest req, UsuarioLogado usuario) {
        PedidoAluguel pedido = obterComAcesso(id, usuario);
        if (usuario.isCliente()) {
            pedido.garantirModificavelPeloCliente();
        } else {
            pedido.garantirModificavelPeloAgente();
        }
        aplicarDados(pedido, req, false);
        return PedidoResponse.de(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long id, UsuarioLogado usuario) {
        if (!usuario.isCliente()) {
            throw new AcessoNegadoException("Somente o cliente pode cancelar o próprio pedido.");
        }
        PedidoAluguel pedido = obterComAcesso(id, usuario);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    // ---------- Agente ----------

    @Transactional
    public PedidoResponse avaliar(Long id, AvaliacaoRequest req, UsuarioLogado usuario) {
        Agente agente = obterAgente(usuario);
        PedidoAluguel pedido = obter(id);
        pedido.avaliar(agente, req.aprovado(), req.parecer().trim());
        return PedidoResponse.de(pedido);
    }

    /**
     * Execução do contrato de um pedido aprovado. Registra a propriedade do automóvel
     * conforme o tipo de contrato e, opcionalmente, associa um contrato de crédito
     * concedido pelo banco agente.
     */
    @Transactional
    public PedidoResponse executarContrato(Long id, ContratoRequest req, UsuarioLogado usuario) {
        Agente agente = obterAgente(usuario);
        PedidoAluguel pedido = obter(id);
        Automovel automovel = pedido.getAutomovel();

        if (!automovel.isDisponivel()) {
            throw new RegraNegocioException("O automóvel deste pedido não está mais disponível.");
        }

        TipoProprietario tipo = req.tipoProprietario();
        Usuario proprietario = switch (tipo) {
            case CLIENTE -> pedido.getCliente();
            case EMPRESA -> {
                exigirTipoAgente(agente, TipoAgente.EMPRESA, "Somente uma empresa pode registrar o automóvel em seu nome.");
                yield agente;
            }
            case BANCO -> {
                exigirTipoAgente(agente, TipoAgente.BANCO, "Somente um banco pode registrar o automóvel em seu nome.");
                yield agente;
            }
        };

        ContratoCredito credito = null;
        if (req.credito() != null) {
            exigirTipoAgente(agente, TipoAgente.BANCO, "Contratos de crédito só podem ser concedidos por bancos.");
            credito = new ContratoCredito(agente, req.credito().valor(), req.credito().taxaJurosMensal(),
                    req.credito().numeroParcelas());
        } else if (pedido.isSolicitaCredito()) {
            throw new RegraNegocioException("Este pedido solicita crédito: o contrato deve ser executado por um banco com os dados do crédito.");
        }

        pedido.marcarComoContratado();
        Contrato contrato = new Contrato(pedido, agente, tipo);
        contrato.setContratoCredito(credito);
        contrato = contratoRepository.save(contrato);
        pedido.setContrato(contrato);
        automovel.registrarPropriedade(tipo, proprietario);

        return PedidoResponse.de(pedido);
    }

    // ---------- auxiliares ----------

    private void aplicarDados(PedidoAluguel pedido, PedidoRequest req, boolean novo) {
        Automovel automovel = automovelService.obter(req.automovelId());
        boolean trocouAutomovel = pedido.getAutomovel() == null || !pedido.getAutomovel().getId().equals(automovel.getId());
        if (trocouAutomovel && !automovel.isDisponivel()) {
            throw new RegraNegocioException("O automóvel selecionado não está disponível para aluguel.");
        }
        if (novo && req.dataInicio() != null && req.dataInicio().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de início não pode estar no passado.");
        }
        pedido.setAutomovel(automovel);
        pedido.setDataInicio(req.dataInicio());
        pedido.setDataFim(req.dataFim());
        pedido.setObservacoes(req.observacoes() == null || req.observacoes().isBlank() ? null : req.observacoes().trim());
        pedido.setSolicitaCredito(req.solicitaCredito());
        if (req.solicitaCredito()) {
            BigDecimal valor = req.valorCreditoSolicitado();
            if (valor == null || valor.signum() <= 0) {
                throw new RegraNegocioException("Informe o valor de crédito desejado.");
            }
            pedido.setValorCreditoSolicitado(valor);
        } else {
            pedido.setValorCreditoSolicitado(null);
        }
        pedido.validarPeriodo();
    }

    private PedidoAluguel obter(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    private PedidoAluguel obterComAcesso(Long id, UsuarioLogado usuario) {
        PedidoAluguel pedido = obter(id);
        if (usuario.isCliente() && !pedido.getCliente().getId().equals(usuario.id())) {
            throw new AcessoNegadoException("Este pedido pertence a outro cliente.");
        }
        return pedido;
    }

    private Agente obterAgente(UsuarioLogado usuario) {
        if (!usuario.isAgente()) {
            throw new AcessoNegadoException("Operação permitida apenas para agentes (empresas e bancos).");
        }
        return agenteRepository.findById(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agente", usuario.id()));
    }

    private static void exigirTipoAgente(Agente agente, TipoAgente tipo, String mensagem) {
        if (agente.getTipo() != tipo) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
