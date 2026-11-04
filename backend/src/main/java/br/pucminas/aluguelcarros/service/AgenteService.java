package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AuthDTO.UsuarioResumo;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consulta dos agentes aderentes (empresas e bancos). */
@Service
public class AgenteService {

    private final AgenteRepository agenteRepository;

    public AgenteService(AgenteRepository agenteRepository) {
        this.agenteRepository = agenteRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumo> listar() {
        return agenteRepository.findAllByOrderByNomeAsc().stream().map(UsuarioResumo::de).toList();
    }
}
