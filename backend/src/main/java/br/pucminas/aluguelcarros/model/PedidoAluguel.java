package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Pedido de aluguel introduzido por um cliente. Após a introdução, é analisado
 * financeiramente por um agente e, se aprovado, fica disponível para execução do contrato.
 * As regras de transição de status ficam encapsuladas nesta entidade.
 */
@Entity
@Table(name = "pedidos_aluguel")
public class PedidoAluguel {

    private static final Set<StatusPedido> CANCELAVEIS = EnumSet.of(StatusPedido.PENDENTE, StatusPedido.APROVADO);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automovel_id", nullable = false)
    private Automovel automovel;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(length = 500)
    private String observacoes;

    /** Indica se o cliente deseja associar o aluguel a um contrato de crédito bancário. */
    @Column(name = "solicita_credito", nullable = false)
    private boolean solicitaCredito;

    @Column(name = "valor_credito_solicitado", precision = 12, scale = 2)
    private BigDecimal valorCreditoSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_avaliador_id")
    private Agente agenteAvaliador;

    @Column(length = 500)
    private String parecer;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY)
    private Contrato contrato;

    public PedidoAluguel() {
    }

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    // ---------- Regras de negócio ----------

    public void validarPeriodo() {
        if (dataInicio == null || dataFim == null) {
            throw new RegraNegocioException("Informe as datas de início e fim do aluguel.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");
        }
    }

    /** Cliente só pode modificar o pedido enquanto ele não foi avaliado. */
    public void garantirModificavelPeloCliente() {
        if (status != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("Só é possível modificar pedidos com status PENDENTE.");
        }
    }

    /** Agente pode modificar pedidos ainda não finalizados (pendentes ou aprovados). */
    public void garantirModificavelPeloAgente() {
        if (status != StatusPedido.PENDENTE && status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Só é possível modificar pedidos PENDENTES ou APROVADOS.");
        }
    }

    public void cancelar() {
        if (!CANCELAVEIS.contains(status)) {
            throw new RegraNegocioException("Pedidos com status " + status + " não podem ser cancelados.");
        }
        status = StatusPedido.CANCELADO;
    }

    /** Análise financeira feita por um agente. */
    public void avaliar(Agente agente, boolean parecerPositivo, String textoParecer) {
        if (status != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("Só é possível avaliar pedidos com status PENDENTE.");
        }
        this.agenteAvaliador = agente;
        this.parecer = textoParecer;
        this.status = parecerPositivo ? StatusPedido.APROVADO : StatusPedido.REPROVADO;
    }

    public void marcarComoContratado() {
        if (status != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Só é possível executar o contrato de pedidos APROVADOS.");
        }
        status = StatusPedido.CONTRATADO;
    }

    public long getQuantidadeDias() {
        if (dataInicio == null || dataFim == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    // ---------- Getters / Setters ----------

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Automovel getAutomovel() { return automovel; }
    public void setAutomovel(Automovel automovel) { this.automovel = automovel; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public boolean isSolicitaCredito() { return solicitaCredito; }
    public void setSolicitaCredito(boolean solicitaCredito) { this.solicitaCredito = solicitaCredito; }
    public BigDecimal getValorCreditoSolicitado() { return valorCreditoSolicitado; }
    public void setValorCreditoSolicitado(BigDecimal valorCreditoSolicitado) { this.valorCreditoSolicitado = valorCreditoSolicitado; }
    public StatusPedido getStatus() { return status; }
    public Agente getAgenteAvaliador() { return agenteAvaliador; }
    public String getParecer() { return parecer; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
}
