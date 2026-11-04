package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.model.enums.Perfil;
import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** DTOs de autenticação e cadastro de agentes. */
public final class AuthDTO {

    private AuthDTO() {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o usuário.") String login,
            @NotBlank(message = "Informe a senha.") String senha) {
    }

    public record UsuarioResumo(Long id, String login, String nome, Perfil perfil, TipoAgente tipoAgente) {
        public static UsuarioResumo de(Usuario u) {
            TipoAgente tipo = (u instanceof Agente a) ? a.getTipo() : null;
            return new UsuarioResumo(u.getId(), u.getLogin(), u.getNomeExibicao(), u.getPerfil(), tipo);
        }
    }

    public record LoginResponse(String token, String tipo, Instant expiraEm, UsuarioResumo usuario) {
    }

    public record RegistroAgenteRequest(
            @NotBlank(message = "Informe o usuário.") @Size(min = 3, max = 60) String login,
            @NotBlank(message = "Informe a senha.") @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.") String senha,
            @NotBlank(message = "Informe o nome.") String nome,
            @NotBlank(message = "Informe o CNPJ.") @Size(max = 18) String cnpj,
            @NotNull(message = "Informe o tipo de agente.") TipoAgente tipo) {
    }
}
