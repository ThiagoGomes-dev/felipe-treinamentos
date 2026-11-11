package br.com.felipetrainer.areamembros.eduzz;

import br.com.felipetrainer.areamembros.aluno.UsuarioService;
import br.com.felipetrainer.areamembros.config.AppProperties;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * Recebe o aviso de venda da Eduzz. Cadastre na Eduzz a URL:
 *   https://SEU-SITE/api/webhooks/eduzz?token=VALOR_DE_EDUZZ_WEBHOOK_TOKEN
 *
 * O código do produto (product_cod) diz qual curso liberar: cadastre esse código no curso, no painel.
 * Aceita tanto o formato antigo (formulário, campos trans_cod / cus_email ...) quanto JSON.
 * Antes de ir para produção, confira os nomes dos campos com um aviso real da conta do
 * Felipe (o log mostra o que chegou) e ajuste {@link EduzzEvento} se precisar.
 */
@RestController
@RequestMapping("/api/webhooks/eduzz")
public class EduzzWebhookController {

    private static final Logger log = LoggerFactory.getLogger(EduzzWebhookController.class);

    private final UsuarioService alunos;
    private final AppProperties props;

    public EduzzWebhookController(UsuarioService alunos, AppProperties props) {
        this.alunos = alunos;
        this.props = props;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> formulario(@RequestParam(required = false) String token,
                                                          @RequestParam MultiValueMap<String, String> campos) {
        return processar(token, EduzzEvento.deFormulario(campos.toSingleValueMap()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> json(@RequestParam(required = false) String token,
                                                    @RequestBody Map<String, Object> corpo) {
        return processar(token, EduzzEvento.deJson(corpo));
    }

    private ResponseEntity<Map<String, String>> processar(String token, EduzzEvento ev) {
        if (!tokenValido(token)) {
            log.warn("Webhook Eduzz recusado: token inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("resultado", "token inválido"));
        }
        log.info("Webhook Eduzz recebido: {}", ev);
        if (ev.transacao() == null || ev.email() == null) {
            // Responde 200 para a Eduzz não ficar reenviando um aviso que não tem o que precisamos.
            return ResponseEntity.ok(Map.of("resultado", "ignorado: sem transação ou e-mail"));
        }
        alunos.processarCompra(ev.transacao(), ev.status(), ev.aprovada(), ev.cancelada(),
                ev.produtoId(), ev.nome(), ev.email(), ev.dataNascimento());
        return ResponseEntity.ok(Map.of("resultado", "ok"));
    }

    private boolean tokenValido(String token) {
        String esperado = props.eduzz().webhookToken();
        return token != null && esperado != null && MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8));
    }
}
