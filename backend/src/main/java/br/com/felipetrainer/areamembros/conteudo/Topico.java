package br.com.felipetrainer.areamembros.conteudo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Subtópico dentro de um módulo: título, descrição leve e vídeos. */
@Entity
public class Topico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Modulo modulo;

    private String titulo;
    private String descricao;
    private int ordem;

    @OneToMany(mappedBy = "topico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    private List<Video> videos = new ArrayList<>();

    public Long getId() { return id; }
    public Modulo getModulo() { return modulo; }
    public void setModulo(Modulo modulo) { this.modulo = modulo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public List<Video> getVideos() { return videos; }

    public Video adicionarVideo(String titulo, String url) {
        Video v = new Video();
        v.setTopico(this);
        v.setTitulo(titulo);
        v.setUrl(url);
        v.setOrdem(videos.size());
        videos.add(v);
        return v;
    }
}
