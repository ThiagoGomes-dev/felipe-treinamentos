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
     * Onde ficam as imagens e vídeos enviados pelo painel: na pasta do servidor ou,
     * se o R2 estiver configurado, no Cloudflare R2.
     */
    public record Armazenamento(String pasta, long maxImagemMb, long maxVideoMb, R2 r2) {}

    /** Cloudflare R2. Fica desligado enquanto contaId ou bucket estiverem vazios. */
    public record R2(String contaId, String bucket, String chaveAcesso, String chaveSecreta) {
        public boolean ativo() {
            return contaId != null && !contaId.isBlank() && bucket != null && !bucket.isBlank();
        }
    }
}
