package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AuthDTO.LoginRequest;
import br.pucminas.aluguelcarros.dto.AuthDTO.LoginResponse;
import br.pucminas.aluguelcarros.dto.AuthDTO.RegistroAgenteRequest;
import br.pucminas.aluguelcarros.dto.AuthDTO.UsuarioResumo;
import br.pucminas.aluguelcarros.dto.ClienteDTO.ClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteDTO.ClienteResponse;
import br.pucminas.aluguelcarros.security.UsuarioLogado;
import br.pucminas.aluguelcarros.service.AuthService;
import br.pucminas.aluguelcarros.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Login e cadastro prévio de usuários (clientes e agentes). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ClienteService clienteService;

    public AuthController(AuthService authService, ClienteService clienteService) {
        this.authService = authService;
        this.clienteService = clienteService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro/cliente")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrarCliente(@Valid @RequestBody ClienteRequest request) {
        return clienteService.cadastrar(request);
    }

    @PostMapping("/registro/agente")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResumo registrarAgente(@Valid @RequestBody RegistroAgenteRequest request) {
        return authService.registrarAgente(request);
    }

    @GetMapping("/me")
    public UsuarioResumo me(@AuthenticationPrincipal Jwt jwt) {
        return authService.usuarioAtual(UsuarioLogado.de(jwt).id());
    }
}
