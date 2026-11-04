package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.dto.AutomovelDTO.AutomovelResponse;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.model.enums.TipoProprietario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTOs de pedidos de aluguel, avaliação e contratos. */
public final class PedidoDTO {

    private PedidoDTO() {
    }

    // ---------- Requests ----------

    public record PedidoRequest(
            @NotNull(message = "Selecione um automóvel.") Long automovelId,
            @NotNull(message = "Informe a data de início.") LocalDate dataInicio,
            @NotNull(message = "Informe a data de fim.") LocalDate dataFim,
            @Size(max = 500) String observacoes,
            boolean solicitaCredito,
            @PositiveOrZero BigDecimal valorCreditoSolicitado) {
    }

    public record AvaliacaoRequest(
            @NotNull(message = "Informe se o parecer é positivo ou negativo.") Boolean aprovado,
            @NotBlank(message = "Descreva o parecer financeiro.") @Size(max = 500) String parecer) {
    }

    public record CreditoRequest(
            @NotNull(message = "Informe o valor do crédito.") @Positive BigDecimal valor,
            @NotNull(message = "Informe a taxa de juros.") @PositiveOrZero @DecimalMax("100.00") BigDecimal taxaJurosMensal,
            @NotNull(message = "Informe o número de parcelas.") @Min(1) @Max(120) Integer numeroParcelas) {
    }

    public record ContratoRequest(
            @NotNull(message = "Informe o tipo de proprietário do automóvel.") TipoProprietario tipoProprietario,
            @Valid CreditoRequest credito) {
    }

    // ---------- Responses ----------

    public record ClienteResumo(Long id, String nome, String cpf, String profissao, BigDecimal rendaTotal) {
    }

    public record CreditoResponse(Long id, String bancoNome, BigDecimal valor, BigDecimal taxaJurosMensal,
                                  Integer numeroParcelas, BigDecimal valorParcela) {
        static CreditoResponse de(ContratoCredito c) {
            if (c == null) return null;
            return new CreditoResponse(c.getId(), c.getBanco().getNome(), c.getValor(), c.getTaxaJurosMensal(),
                    c.getNumeroParcelas(), c.getValorParcela());
        }
    }

    public record ContratoResponse(Long id, TipoProprietario tipoProprietario, String agenteNome,
                                   LocalDateTime dataAssinatura, CreditoResponse credito) {
        static ContratoResponse de(Contrato c) {
            if (c == null) return null;
            return new ContratoResponse(c.getId(), c.getTipoProprietario(), c.getAgente().getNome(),
                    c.getDataAssinatura(), CreditoResponse.de(c.getContratoCredito()));
        }
    }

    public record PedidoResponse(
            Long id, StatusPedido status, LocalDate dataInicio, LocalDate dataFim, long quantidadeDias,
            String observacoes, boolean solicitaCredito, BigDecimal valorCreditoSolicitado,
            LocalDateTime criadoEm, LocalDateTime atualizadoEm,
            ClienteResumo cliente, AutomovelResponse automovel,
            String agenteAvaliador, String parecer, ContratoResponse contrato) {

        public static PedidoResponse de(PedidoAluguel p) {
            var c = p.getCliente();
            var cliente = new ClienteResumo(c.getId(), c.getNome(), c.getCpf(), c.getProfissao(), c.getRendaTotal());
            String avaliador = p.getAgenteAvaliador() != null ? p.getAgenteAvaliador().getNome() : null;
            return new PedidoResponse(p.getId(), p.getStatus(), p.getDataInicio(), p.getDataFim(),
                    p.getQuantidadeDias(), p.getObservacoes(), p.isSolicitaCredito(), p.getValorCreditoSolicitado(),
                    p.getCriadoEm(), p.getAtualizadoEm(), cliente, AutomovelResponse.de(p.getAutomovel()),
                    avaliador, p.getParecer(), ContratoResponse.de(p.getContrato()));
        }
    }
}
