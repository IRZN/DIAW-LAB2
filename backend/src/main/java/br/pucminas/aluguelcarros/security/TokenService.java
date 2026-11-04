package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.dto.AuthDTO.LoginResponse;
import br.pucminas.aluguelcarros.dto.AuthDTO.UsuarioResumo;
import br.pucminas.aluguelcarros.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Emite tokens JWT (HS256) após a autenticação com usuário e senha. */
@Service
public class TokenService {

    public static final String ISSUER = "aluguel-carros-api";
    public static final String CLAIM_LOGIN = "login";
    public static final String CLAIM_PERFIL = "perfil";
    public static final String CLAIM_ROLES = "roles";

    private final JwtEncoder encoder;
    private final long expiracaoHoras;

    public TokenService(JwtEncoder encoder, @Value("${app.jwt.expiracao-horas:8}") long expiracaoHoras) {
        this.encoder = encoder;
        this.expiracaoHoras = expiracaoHoras;
    }

    public LoginResponse gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(expiracaoHoras, ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(agora)
                .expiresAt(expira)
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_LOGIN, usuario.getLogin())
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .claim(CLAIM_ROLES, List.of("ROLE_" + usuario.getPerfil().name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expira, UsuarioResumo.de(usuario));
    }
}
