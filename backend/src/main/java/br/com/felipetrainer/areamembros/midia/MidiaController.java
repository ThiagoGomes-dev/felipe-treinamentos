package br.com.felipetrainer.areamembros.midia;

import java.net.URI;
import java.time.Duration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Entrega as imagens e vídeos guardados no servidor. Imagens são abertas; vídeos exigem o
 * link assinado. O Spring responde a pedidos parciais (Range), então dá para avançar o vídeo.
 */
@RestController
public class MidiaController {

    private final ArmazenamentoService armazenamento;
    private final MidiaTokenService tokens;

    public MidiaController(ArmazenamentoService armazenamento, MidiaTokenService tokens) {
        this.armazenamento = armazenamento;
        this.tokens = tokens;
    }

    @GetMapping("/api/midia/{id}")
    public ResponseEntity<Resource> midia(@PathVariable String id, @RequestParam(name = "t", required = false) String token) {
        Arquivo a = armazenamento.buscar(id).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();
        if (!a.isImagem() && !tokens.valido(id, token)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (armazenamento.usaR2()) {
            // O arquivo vem direto do R2 por um link temporário (o servidor não gasta banda).
            Duration validade = a.isImagem() ? Duration.ofDays(1) : Duration.ofHours(6);
            CacheControl cacheRedirect = a.isImagem()
                    ? CacheControl.maxAge(Duration.ofHours(12)).cachePublic()
                    : CacheControl.noStore();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(armazenamento.linkR2(a, validade)))
                    .cacheControl(cacheRedirect)
                    .build();
        }

        FileSystemResource arquivo = new FileSystemResource(armazenamento.caminho(a));
        if (!arquivo.exists()) return ResponseEntity.notFound().build();

        CacheControl cache = a.isImagem()
                ? CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable()
                : CacheControl.maxAge(Duration.ofHours(1)).cachePrivate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .cacheControl(cache)
                .body(arquivo);
    }
}
