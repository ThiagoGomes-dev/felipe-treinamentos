package br.com.felipetrainer.areamembros.midia;

import br.com.felipetrainer.areamembros.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Gera links temporários para os vídeos. O player do navegador não manda o token de login,
 * então o link do vídeo leva uma assinatura que vale por algumas horas. Quem não tem o curso
 * liberado nunca recebe esse link.
 */
@Service
public class MidiaTokenService {

    private static final long VALIDADE_SEGUNDOS = 6 * 60 * 60;

    private final byte[] segredo;

    public MidiaTokenService(AppProperties props) {
        this.segredo = ("midia:" + props.jwt().segredo()).getBytes(StandardCharsets.UTF_8);
    }

    /** URL pública da imagem (capas não são secretas). */
    public static String urlImagem(String arquivoId) {
        return arquivoId == null ? null : "/api/midia/" + arquivoId;
    }

    /** URL assinada do vídeo. */
    public String urlVideo(String arquivoId) {
        long exp = Instant.now().getEpochSecond() + VALIDADE_SEGUNDOS;
        return "/api/midia/" + arquivoId + "?t=" + exp + "." + assinar(arquivoId, exp);
    }

    public boolean valido(String arquivoId, String token) {
        if (token == null) return false;
        int ponto = token.indexOf('.');
        if (ponto < 1) return false;
        long exp;
        try {
            exp = Long.parseLong(token.substring(0, ponto));
        } catch (NumberFormatException e) {
            return false;
        }
        if (exp < Instant.now().getEpochSecond()) return false;
        byte[] esperado = assinar(arquivoId, exp).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, token.substring(ponto + 1).getBytes(StandardCharsets.UTF_8));
    }

    private String assinar(String arquivoId, long exp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo, "HmacSHA256"));
            byte[] sig = mac.doFinal((arquivoId + ":" + exp).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
