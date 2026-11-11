package br.com.felipetrainer.areamembros.midia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Imagem ou vídeo enviado pelo painel, guardado no disco do servidor ou no banco (campo dados). */
@Entity
public class Arquivo {

    @Id
    private String id;
    private String nomeOriginal;
    private String contentType;
    private long tamanho;
    /** Caminho relativo à pasta de armazenamento. */
    private String caminho;
    /** Conteúdo do arquivo quando ele fica no banco; nulo quando fica no disco. */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] dados;
    private Instant criadoEm = Instant.now();

    protected Arquivo() {}

    public Arquivo(String id, String nomeOriginal, String contentType, long tamanho, String caminho, byte[] dados) {
        this.id = id;
        this.nomeOriginal = nomeOriginal;
        this.contentType = contentType;
        this.tamanho = tamanho;
        this.caminho = caminho;
        this.dados = dados;
    }

    public String getId() { return id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getContentType() { return contentType; }
    public long getTamanho() { return tamanho; }
    public String getCaminho() { return caminho; }
    public Instant getCriadoEm() { return criadoEm; }
    public byte[] getDados() { return dados; }
    public boolean isNoBanco() { return dados != null; }

    public boolean isImagem() {
        return contentType.startsWith("image/");
    }
}
