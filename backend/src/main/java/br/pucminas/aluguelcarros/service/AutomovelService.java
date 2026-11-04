package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AutomovelDTO.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.AutomovelDTO.AutomovelResponse;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Cadastro e consulta de automóveis. */
@Service
public class AutomovelService {

    private final AutomovelRepository automovelRepository;
    private final PedidoAluguelRepository pedidoRepository;

    public AutomovelService(AutomovelRepository automovelRepository, PedidoAluguelRepository pedidoRepository) {
        this.automovelRepository = automovelRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar(boolean apenasDisponiveis) {
        var lista = apenasDisponiveis
                ? automovelRepository.findByDisponivelTrueOrderByMarcaAscModeloAsc()
                : automovelRepository.findAllByOrderByMarcaAscModeloAsc();
        return lista.stream().map(AutomovelResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AutomovelResponse buscar(Long id) {
        return AutomovelResponse.de(obter(id));
    }

    @Transactional
    public AutomovelResponse criar(AutomovelRequest req) {
        String placa = normalizarPlaca(req.placa());
        if (automovelRepository.existsByPlacaIgnoreCase(placa)) {
            throw new RegraNegocioException("Já existe um automóvel com a placa informada.");
        }
        if (automovelRepository.existsByMatriculaIgnoreCase(req.matricula().trim())) {
            throw new RegraNegocioException("Já existe um automóvel com a matrícula informada.");
        }
        Automovel a = new Automovel(req.matricula().trim(), req.ano(), req.marca().trim(), req.modelo().trim(), placa);
        return AutomovelResponse.de(automovelRepository.save(a));
    }

    @Transactional
    public AutomovelResponse atualizar(Long id, AutomovelRequest req) {
        Automovel a = obter(id);
        String placa = normalizarPlaca(req.placa());
        if (!placa.equalsIgnoreCase(a.getPlaca()) && automovelRepository.existsByPlacaIgnoreCase(placa)) {
            throw new RegraNegocioException("Já existe um automóvel com a placa informada.");
        }
        if (!req.matricula().trim().equalsIgnoreCase(a.getMatricula())
                && automovelRepository.existsByMatriculaIgnoreCase(req.matricula().trim())) {
            throw new RegraNegocioException("Já existe um automóvel com a matrícula informada.");
        }
        a.setMatricula(req.matricula().trim());
        a.setAno(req.ano());
        a.setMarca(req.marca().trim());
        a.setModelo(req.modelo().trim());
        a.setPlaca(placa);
        return AutomovelResponse.de(a);
    }

    @Transactional
    public void excluir(Long id) {
        Automovel a = obter(id);
        if (pedidoRepository.existsByAutomovelId(id)) {
            throw new RegraNegocioException("Não é possível excluir: o automóvel possui pedidos vinculados.");
        }
        automovelRepository.delete(a);
    }

    Automovel obter(Long id) {
        return automovelRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel", id));
    }

    private static String normalizarPlaca(String placa) {
        return placa.replace("-", "").trim().toUpperCase();
    }
}
