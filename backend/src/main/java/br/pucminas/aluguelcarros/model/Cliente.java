package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.Perfil;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Contratante do aluguel (usuário individual). Armazena dados de identificação
 * (RG, CPF, Nome, Endereço), profissão e até 3 rendimentos (entidade empregadora + valor).
 */
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario {

    public static final int MAX_RENDIMENTOS = 3;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column(length = 80)
    private String profissao;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Rendimento> rendimentos = new ArrayList<>();

    public Cliente() {
        setPerfil(Perfil.CLIENTE);
    }

    public Cliente(String login, String senha, String rg, String cpf, String nome, String endereco, String profissao) {
        super(login, senha, Perfil.CLIENTE);
        this.rg = rg;
        this.cpf = cpf;
        this.nome = nome;
        this.endereco = endereco;
        this.profissao = profissao;
    }

    /** Substitui os rendimentos, respeitando o limite de 3. */
    public void definirRendimentos(List<Rendimento> novos) {
        if (novos != null && novos.size() > MAX_RENDIMENTOS) {
            throw new IllegalArgumentException("Um cliente pode ter no máximo " + MAX_RENDIMENTOS + " rendimentos.");
        }
        rendimentos.clear();
        if (novos != null) {
            novos.forEach(r -> {
                r.setCliente(this);
                rendimentos.add(r);
            });
        }
    }

    public BigDecimal getRendaTotal() {
        return rendimentos.stream()
                .map(Rendimento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String getNomeExibicao() {
        return nome;
    }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }
    public List<Rendimento> getRendimentos() { return rendimentos; }
}
