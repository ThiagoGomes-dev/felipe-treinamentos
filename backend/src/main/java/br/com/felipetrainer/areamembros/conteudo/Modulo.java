package br.com.felipetrainer.areamembros.conteudo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Um card dentro do curso: um tipo de exercício (ex.: "Joelho forte"). */
@Entity
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Curso curso;

    private String slug;
    private String titulo;
    private String descricao;
    /** Imagem do card (id do arquivo enviado). */
    private String capaId;
    private int ordem;
    private boolean publicado = true;

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    private List<Topico> topicos = new ArrayList<>();

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCapaId() { return capaId; }
    public void setCapaId(String capaId) { this.capaId = capaId; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public boolean isPublicado() { return publicado; }
    public void setPublicado(boolean publicado) { this.publicado = publicado; }
    public List<Topico> getTopicos() { return topicos; }

    public Topico adicionarTopico(String titulo, String descricao) {
        Topico t = new Topico();
        t.setModulo(this);
        t.setTitulo(titulo);
        t.setDescricao(descricao);
        t.setOrdem(topicos.size());
        topicos.add(t);
        return t;
    }
}
