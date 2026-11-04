package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.Perfil;
import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import jakarta.persistence.*;

/**
 * Agente aderente (empresa ou banco). Pode modificar e avaliar pedidos de aluguel.
 * Bancos podem conceder contratos de crédito.
 */
@Entity
@Table(name = "agentes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Agente extends Usuario {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAgente tipo;

    public Agente() {
        setPerfil(Perfil.AGENTE);
    }

    public Agente(String login, String senha, String nome, String cnpj, TipoAgente tipo) {
        super(login, senha, Perfil.AGENTE);
        this.nome = nome;
        this.cnpj = cnpj;
        this.tipo = tipo;
    }

    public boolean isBanco() {
        return tipo == TipoAgente.BANCO;
    }

    @Override
    public String getNomeExibicao() {
        return nome;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public TipoAgente getTipo() { return tipo; }
    public void setTipo(TipoAgente tipo) { this.tipo = tipo; }
}
