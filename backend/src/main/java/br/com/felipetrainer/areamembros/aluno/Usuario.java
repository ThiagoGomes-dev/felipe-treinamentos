package br.com.felipetrainer.areamembros.aluno;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Usuario {

    public enum Origem { MANUAL, EDUZZ }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String senhaHash;
    private LocalDate dataNascimento;
    /** Enquanto for true, o aluno é levado para a tela de troca de senha ao entrar. */
    private boolean senhaProvisoria = true;
    @Enumerated(EnumType.STRING)
    private Papel papel = Papel.ALUNO;
    private boolean ativo = true;
    @Enumerated(EnumType.STRING)
    private Origem origem = Origem.MANUAL;
    private Instant criadoEm = Instant.now();

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public boolean isSenhaProvisoria() { return senhaProvisoria; }
    public void setSenhaProvisoria(boolean senhaProvisoria) { this.senhaProvisoria = senhaProvisoria; }
    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Origem getOrigem() { return origem; }
    public void setOrigem(Origem origem) { this.origem = origem; }
    public Instant getCriadoEm() { return criadoEm; }
}
