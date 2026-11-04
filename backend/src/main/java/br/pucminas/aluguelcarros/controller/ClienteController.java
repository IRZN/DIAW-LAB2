package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.ClienteDTO.ClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteDTO.ClienteResponse;
import br.pucminas.aluguelcarros.security.UsuarioLogado;
import br.pucminas.aluguelcarros.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD de cliente. O "Create" é feito pelo cadastro público em
 * {@code POST /api/auth/registro/cliente}; aqui ficam Read, Update e Delete.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENTE')")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse meusDados(@AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado u = UsuarioLogado.de(jwt);
        return clienteService.buscar(u.id(), u);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return clienteService.buscar(id, UsuarioLogado.de(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return clienteService.atualizar(id, request, UsuarioLogado.de(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        clienteService.excluir(id, UsuarioLogado.de(jwt));
    }
}
