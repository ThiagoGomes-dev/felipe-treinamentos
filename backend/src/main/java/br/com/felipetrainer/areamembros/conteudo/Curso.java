package br.com.felipetrainer.areamembros.conteudo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Um curso/produto (ex.: "Fortalecimento para Atletas"). É liberado por usuário. */
@Entity
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String slug;
    private String titulo;
    private String subtitulo;
    private String descricao;
    /** Imagem de capa (id do arquivo enviado). */
    private String capaId;
    /** Código do produto na Eduzz: a compra desse produto libera este curso. */
    private String eduzzProdutoId;
    /** Página de venda, mostrada para quem ainda não tem o curso. */
    private String linkVenda;
    private int ordem;
    private boolean publicado = true;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    private List<Modulo> modulos = new ArrayList<>();

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getSubtitulo() { return subtitulo; }
    public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCapaId() { return capaId; }
    public void setCapaId(String capaId) { this.capaId = capaId; }
    public String getEduzzProdutoId() { return eduzzProdutoId; }
    public void setEduzzProdutoId(String eduzzProdutoId) { this.eduzzProdutoId = eduzzProdutoId; }
    public String getLinkVenda() { return linkVenda; }
    public void setLinkVenda(String linkVenda) { this.linkVenda = linkVenda; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public boolean isPublicado() { return publicado; }
    public void setPublicado(boolean publicado) { this.publicado = publicado; }
    public List<Modulo> getModulos() { return modulos; }

    public Modulo adicionarModulo(String slug, String titulo, String descricao) {
        Modulo m = new Modulo();
        m.setCurso(this);
        m.setSlug(slug);
        m.setTitulo(titulo);
        m.setDescricao(descricao);
        m.setOrdem(modulos.size());
        modulos.add(m);
        return m;
    }
}
