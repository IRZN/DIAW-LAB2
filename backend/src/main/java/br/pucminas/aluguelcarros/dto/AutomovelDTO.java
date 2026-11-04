package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.enums.TipoProprietario;
import jakarta.validation.constraints.*;

/** DTOs de automóvel. */
public final class AutomovelDTO {

    private AutomovelDTO() {
    }

    public record AutomovelRequest(
            @NotBlank(message = "Informe a matrícula (RENAVAM).") @Size(max = 30) String matricula,
            @NotNull(message = "Informe o ano.") @Min(value = 1950, message = "Ano inválido.") @Max(value = 2100, message = "Ano inválido.") Integer ano,
            @NotBlank(message = "Informe a marca.") @Size(max = 60) String marca,
            @NotBlank(message = "Informe o modelo.") @Size(max = 60) String modelo,
            @NotBlank(message = "Informe a placa.")
            @Pattern(regexp = "[A-Za-z]{3}-?\\d[A-Za-z0-9]\\d{2}", message = "Placa inválida (ex.: ABC1D23 ou ABC-1234).") String placa) {
    }

    public record AutomovelResponse(
            Long id, String matricula, Integer ano, String marca, String modelo, String placa,
            boolean disponivel, TipoProprietario tipoProprietario, String proprietarioNome) {

        public static AutomovelResponse de(Automovel a) {
            String dono = a.getProprietario() != null ? a.getProprietario().getNomeExibicao() : null;
            return new AutomovelResponse(a.getId(), a.getMatricula(), a.getAno(), a.getMarca(), a.getModelo(),
                    a.getPlaca(), a.isDisponivel(), a.getTipoProprietario(), dono);
        }
    }
}
