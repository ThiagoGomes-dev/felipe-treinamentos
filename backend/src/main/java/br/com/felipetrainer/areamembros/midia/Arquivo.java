package br.com.felipetrainer.areamembros.midia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

/** Imagem ou vídeo enviado pelo painel e guardado no disco do servidor. */
@Entity
public class Arquivo {

    @Id
    private String id;
    private String nomeOriginal;
    private String contentType;
    private long tamanho;
    /** Caminho relativo à pasta de armazenamento. */
    private String caminho;
    private Instant criadoEm = Instant.now();

    protected Arquivo() {}

    public Arquivo(String id, String nomeOriginal, String contentType, long tamanho, String caminho) {
        this.id = id;
        this.nomeOriginal = nomeOriginal;
        this.contentType = contentType;
        this.tamanho = tamanho;
        this.caminho = caminho;
    }

    public String getId() { return id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getContentType() { return contentType; }
    public long getTamanho() { return tamanho; }
    public String getCaminho() { return caminho; }
    public Instant getCriadoEm() { return criadoEm; }

    public boolean isImagem() {
        return contentType.startsWith("image/");
    }
}
