package br.pucminas.aluguelcarros.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Contrato de crédito concedido por um banco agente e associado a um aluguel. */
@Entity
@Table(name = "contratos_credito")
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Agente banco;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Taxa de juros mensal em percentual (ex.: 1.5 = 1,5% a.m.). */
    @Column(name = "taxa_juros_mensal", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaJurosMensal;

    @Column(name = "numero_parcelas", nullable = false)
    private Integer numeroParcelas;

    public ContratoCredito() {
    }

    public ContratoCredito(Agente banco, BigDecimal valor, BigDecimal taxaJurosMensal, Integer numeroParcelas) {
        this.banco = banco;
        this.valor = valor;
        this.taxaJurosMensal = taxaJurosMensal;
        this.numeroParcelas = numeroParcelas;
    }

    /** Valor da parcela pela Tabela Price. */
    public BigDecimal getValorParcela() {
        if (valor == null || numeroParcelas == null || numeroParcelas <= 0) return BigDecimal.ZERO;
        double i = taxaJurosMensal.doubleValue() / 100.0;
        double pv = valor.doubleValue();
        int n = numeroParcelas;
        double parcela = (i == 0) ? pv / n : pv * i / (1 - Math.pow(1 + i, -n));
        return BigDecimal.valueOf(parcela).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Agente getBanco() { return banco; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getTaxaJurosMensal() { return taxaJurosMensal; }
    public Integer getNumeroParcelas() { return numeroParcelas; }
}
