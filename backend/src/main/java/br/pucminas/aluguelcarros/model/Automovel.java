package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.TipoProprietario;
import jakarta.persistence.*;

/**
 * Automóvel disponível para aluguel. Registra matrícula, ano, marca, modelo e placa.
 * Após a execução de um contrato, é registrado como propriedade de um cliente, empresa ou banco.
 */
@Entity
@Table(name = "automoveis")
public class Automovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String matricula;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_proprietario", length = 20)
    private TipoProprietario tipoProprietario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietario_id")
    private Usuario proprietario;

    @Column(nullable = false)
    private boolean disponivel = true;

    public Automovel() {
    }

    public Automovel(String matricula, Integer ano, String marca, String modelo, String placa) {
        this.matricula = matricula;
        this.ano = ano;
        this.marca = marca;
        this.modelo = modelo;
        this.placa = placa;
    }

    /** Registra a propriedade do automóvel conforme o tipo de contrato. */
    public void registrarPropriedade(TipoProprietario tipo, Usuario novoProprietario) {
        this.tipoProprietario = tipo;
        this.proprietario = novoProprietario;
        this.disponivel = false;
    }

    public Long getId() { return id; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public TipoProprietario getTipoProprietario() { return tipoProprietario; }
    public Usuario getProprietario() { return proprietario; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
}
