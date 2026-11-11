package br.com.felipetrainer.areamembros.conteudo;

import jakarta.persistence.*;

/** Configurações gerais do site. */
@Entity
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String slogan;
    private String corDestaque = "#e3161b";

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSlogan() { return slogan; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
    public String getCorDestaque() { return corDestaque; }
    public void setCorDestaque(String corDestaque) { this.corDestaque = corDestaque; }
}
