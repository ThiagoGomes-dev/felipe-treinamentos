package br.com.felipetrainer.areamembros.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(
        String urlPublica,
        String emailRemetente,
        Jwt jwt,
        Admin admin,
        Eduzz eduzz,
        Armazenamento armazenamento,
        boolean dadosExemplo,
        String corsOrigens) {

    public record Jwt(String segredo, int horasValidade) {}

    public record Admin(String email, String senha) {}

    public record Eduzz(String webhookToken) {}

    /**
     * Onde ficam as imagens e vídeos enviados pelo painel.
     * imagensNoBanco: guarda as imagens no próprio banco (para servidores sem disco fixo, como o Render grátis).
     * uploadVideo: false bloqueia o envio de arquivos de vídeo (aí os vídeos entram por link do YouTube/Vimeo).
     */
    public record Armazenamento(String pasta, long maxImagemMb, long maxVideoMb, boolean imagensNoBanco, boolean uploadVideo) {}
}
