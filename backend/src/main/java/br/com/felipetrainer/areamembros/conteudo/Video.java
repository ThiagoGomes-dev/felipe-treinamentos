package br.com.felipetrainer.areamembros.conteudo;

import jakarta.persistence.*;

@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Topico topico;

    private String titulo;
    private String descricao;
    /** Vídeo enviado ao servidor (id do arquivo). Tem prioridade sobre a url. */
    private String arquivoId;
    /** Alternativa: link do YouTube, Vimeo ou .mp4 externo. */
    private String url;
    private int ordem;

    public Long getId() { return id; }
    public Topico getTopico() { return topico; }
    public void setTopico(Topico topico) { this.topico = topico; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getArquivoId() { return arquivoId; }
    public void setArquivoId(String arquivoId) { this.arquivoId = arquivoId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}
