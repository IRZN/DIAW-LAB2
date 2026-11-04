package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AuthDTO.UsuarioResumo;
import br.pucminas.aluguelcarros.service.AgenteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Consulta de agentes aderentes e endpoint de status da API. */
@RestController
@RequestMapping("/api")
public class AgenteController {

    private final AgenteService agenteService;

    public AgenteController(AgenteService agenteService) {
        this.agenteService = agenteService;
    }

    @GetMapping("/agentes")
    public List<UsuarioResumo> listar() {
        return agenteService.listar();
    }

    /** Rota pública usada para verificar se a API está no ar. */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("status", "UP", "aplicacao", "Aluguel de Carros API", "horario", Instant.now().toString());
    }
}
