package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.AuthDTO.LoginRequest;
import br.pucminas.aluguelcarros.dto.AuthDTO.LoginResponse;
import br.pucminas.aluguelcarros.dto.AuthDTO.RegistroAgenteRequest;
import br.pucminas.aluguelcarros.dto.AuthDTO.UsuarioResumo;
import br.pucminas.aluguelcarros.exception.CredenciaisInvalidasException;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Autenticação (login com usuário e senha) e cadastro de agentes. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AgenteRepository agenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, AgenteRepository agenteRepository,
                       PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.agenteRepository = agenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByLogin(request.login().trim())
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }
        return tokenService.gerar(usuario);
    }

    @Transactional
    public UsuarioResumo registrarAgente(RegistroAgenteRequest request) {
        String login = request.login().trim();
        if (usuarioRepository.existsByLogin(login)) {
            throw new RegraNegocioException("Já existe um usuário com o login informado.");
        }
        if (agenteRepository.existsByCnpj(request.cnpj().trim())) {
            throw new RegraNegocioException("Já existe um agente com o CNPJ informado.");
        }
        Agente agente = new Agente(login, passwordEncoder.encode(request.senha()),
                request.nome().trim(), request.cnpj().trim(), request.tipo());
        return UsuarioResumo.de(agenteRepository.save(agente));
    }

    @Transactional(readOnly = true)
    public UsuarioResumo usuarioAtual(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
        return UsuarioResumo.de(usuario);
    }
}
