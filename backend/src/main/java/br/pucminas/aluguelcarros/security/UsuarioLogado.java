package br.pucminas.aluguelcarros.security;

import br.pucminas.aluguelcarros.model.enums.Perfil;
import org.springframework.security.oauth2.jwt.Jwt;

/** Identidade do usuário autenticado, extraída do token JWT. */
public record UsuarioLogado(Long id, String login, Perfil perfil) {

    public static UsuarioLogado de(Jwt jwt) {
        return new UsuarioLogado(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(TokenService.CLAIM_LOGIN),
                Perfil.valueOf(jwt.getClaimAsString(TokenService.CLAIM_PERFIL)));
    }

    public boolean isCliente() {
        return perfil == Perfil.CLIENTE;
    }

    public boolean isAgente() {
        return perfil == Perfil.AGENTE;
    }
}
