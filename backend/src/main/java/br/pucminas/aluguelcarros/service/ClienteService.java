package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.ClienteDTO.ClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteDTO.ClienteResponse;
import br.pucminas.aluguelcarros.exception.AcessoNegadoException;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.UsuarioLogado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras do CRUD de cliente (contratante do aluguel). */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoAluguelRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
                          PedidoAluguelRepository pedidoRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Cadastro prévio do cliente (rota pública). */
    @Transactional
    public ClienteResponse cadastrar(ClienteRequest req) {
        if (req.senha() == null || req.senha().length() < 6) {
            throw new RegraNegocioException("A senha deve ter no mínimo 6 caracteres.");
        }
        String login = req.login().trim();
        String cpf = normalizarCpf(req.cpf());
        if (usuarioRepository.existsByLogin(login)) {
            throw new RegraNegocioException("Já existe um usuário com o login informado.");
        }
        if (clienteRepository.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe um cliente com o CPF informado.");
        }
        Cliente cliente = new Cliente(login, passwordEncoder.encode(req.senha()), req.rg().trim(), cpf,
                req.nome().trim(), req.endereco().trim(), limpar(req.profissao()));
        cliente.definirRendimentos(converterRendimentos(req));
        return ClienteResponse.de(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNomeAsc().stream().map(ClienteResponse::de).toList();
    }

    /** Cliente vê apenas os próprios dados; agentes podem consultar qualquer cliente (análise financeira). */
    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id, UsuarioLogado usuario) {
        if (usuario.isCliente() && !usuario.id().equals(id)) {
            throw new AcessoNegadoException("Você só pode consultar os seus próprios dados.");
        }
        return ClienteResponse.de(obter(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest req, UsuarioLogado usuario) {
        garantirProprioCliente(id, usuario);
        Cliente cliente = obter(id);

        String login = req.login().trim();
        if (!login.equals(cliente.getLogin()) && usuarioRepository.existsByLogin(login)) {
            throw new RegraNegocioException("Já existe um usuário com o login informado.");
        }
        String cpf = normalizarCpf(req.cpf());
        if (clienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new RegraNegocioException("Já existe um cliente com o CPF informado.");
        }

        cliente.setLogin(login);
        cliente.setRg(req.rg().trim());
        cliente.setCpf(cpf);
        cliente.setNome(req.nome().trim());
        cliente.setEndereco(req.endereco().trim());
        cliente.setProfissao(limpar(req.profissao()));
        cliente.definirRendimentos(converterRendimentos(req));
        if (req.senha() != null && !req.senha().isBlank()) {
            if (req.senha().length() < 6) {
                throw new RegraNegocioException("A senha deve ter no mínimo 6 caracteres.");
            }
            cliente.setSenha(passwordEncoder.encode(req.senha()));
        }
        return ClienteResponse.de(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Long id, UsuarioLogado usuario) {
        garantirProprioCliente(id, usuario);
        Cliente cliente = obter(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException("Não é possível excluir a conta: existem pedidos de aluguel vinculados.");
        }
        clienteRepository.delete(cliente);
    }

    // ---------- auxiliares ----------

    private Cliente obter(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    private void garantirProprioCliente(Long id, UsuarioLogado usuario) {
        if (!usuario.isCliente() || !usuario.id().equals(id)) {
            throw new AcessoNegadoException("Somente o próprio cliente pode alterar ou excluir o seu cadastro.");
        }
    }

    private List<Rendimento> converterRendimentos(ClienteRequest req) {
        if (req.rendimentos() == null) return List.of();
        if (req.rendimentos().size() > Cliente.MAX_RENDIMENTOS) {
            throw new RegraNegocioException("Informe no máximo " + Cliente.MAX_RENDIMENTOS + " rendimentos.");
        }
        return req.rendimentos().stream()
                .map(r -> new Rendimento(r.entidadeEmpregadora().trim(), r.valor()))
                .toList();
    }

    /** Armazena o CPF no formato 000.000.000-00. */
    static String normalizarCpf(String cpf) {
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new RegraNegocioException("CPF deve conter 11 dígitos.");
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    private static String limpar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
