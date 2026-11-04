package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs do CRUD de cliente. */
public final class ClienteDTO {

    private ClienteDTO() {
    }

    public record RendimentoDTO(
            @NotBlank(message = "Informe a entidade empregadora.") @Size(max = 120) String entidadeEmpregadora,
            @NotNull(message = "Informe o valor do rendimento.") @PositiveOrZero BigDecimal valor) {
    }

    /**
     * Usado no cadastro e na atualização. Na atualização a senha é opcional
     * (se vier em branco, a senha atual é mantida).
     */
    public record ClienteRequest(
            @NotBlank(message = "Informe o usuário.") @Size(min = 3, max = 60) String login,
            @Size(max = 72) String senha,
            @NotBlank(message = "Informe o RG.") @Size(max = 20) String rg,
            @NotBlank(message = "Informe o CPF.") @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF em formato inválido.") String cpf,
            @NotBlank(message = "Informe o nome.") @Size(max = 120) String nome,
            @NotBlank(message = "Informe o endereço.") @Size(max = 255) String endereco,
            @Size(max = 80) String profissao,
            @Size(max = 3, message = "Informe no máximo 3 rendimentos.") List<@Valid RendimentoDTO> rendimentos) {
    }

    public record ClienteResponse(
            Long id, String login, String rg, String cpf, String nome, String endereco, String profissao,
            List<RendimentoDTO> rendimentos, BigDecimal rendaTotal, LocalDateTime criadoEm) {

        public static ClienteResponse de(Cliente c) {
            List<RendimentoDTO> rends = c.getRendimentos().stream()
                    .map(r -> new RendimentoDTO(r.getEntidadeEmpregadora(), r.getValor()))
                    .toList();
            return new ClienteResponse(c.getId(), c.getLogin(), c.getRg(), c.getCpf(), c.getNome(),
                    c.getEndereco(), c.getProfissao(), rends, c.getRendaTotal(), c.getCriadoEm());
        }
    }
}
