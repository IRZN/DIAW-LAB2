package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.TipoProprietario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Contrato de aluguel executado por um agente a partir de um pedido aprovado.
 * O tipo de contrato define quem será o proprietário registrado do automóvel
 * e pode estar associado a um {@link ContratoCredito} concedido por um banco.
 */
@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoAluguel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_proprietario", nullable = false, length = 20)
    private TipoProprietario tipoProprietario;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_credito_id")
    private ContratoCredito contratoCredito;

    @Column(name = "data_assinatura", nullable = false)
    private LocalDateTime dataAssinatura;

    public Contrato() {
    }

    public Contrato(PedidoAluguel pedido, Agente agente, TipoProprietario tipoProprietario) {
        this.pedido = pedido;
        this.agente = agente;
        this.tipoProprietario = tipoProprietario;
        this.dataAssinatura = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public PedidoAluguel getPedido() { return pedido; }
    public Agente getAgente() { return agente; }
    public TipoProprietario getTipoProprietario() { return tipoProprietario; }
    public ContratoCredito getContratoCredito() { return contratoCredito; }
    public void setContratoCredito(ContratoCredito contratoCredito) { this.contratoCredito = contratoCredito; }
    public LocalDateTime getDataAssinatura() { return dataAssinatura; }
}
