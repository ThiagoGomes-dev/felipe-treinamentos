package br.com.felipetrainer.areamembros.midia;

import br.com.felipetrainer.areamembros.config.AppProperties;
import br.com.felipetrainer.areamembros.web.RegraException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda imagens e vídeos no Cloudflare R2 (se configurado) ou numa pasta do servidor
 * (app.armazenamento.pasta). Os arquivos ficam em AAAA/MM/uuid.ext e o banco guarda só os dados.
 */
@Service
public class ArmazenamentoService {

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoService.class);

    public enum Tipo { IMAGEM, VIDEO }

    private static final Map<String, String> IMAGENS = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp", "image/gif", "gif", "image/avif", "avif");
    private static final Map<String, String> VIDEOS = Map.of(
            "video/mp4", "mp4", "video/webm", "webm", "video/quicktime", "mov");

    private final ArquivoRepository arquivos;
    private final Path raiz;
    private final long maxImagem;
    private final long maxVideo;
    /** Nulo quando os arquivos ficam no disco do servidor. */
    private final ArmazenamentoR2 r2;

    public ArmazenamentoService(ArquivoRepository arquivos, AppProperties props) throws IOException {
        this.arquivos = arquivos;
        this.raiz = Path.of(props.armazenamento().pasta()).toAbsolutePath().normalize();
        this.maxImagem = props.armazenamento().maxImagemMb() * 1024 * 1024;
        this.maxVideo = props.armazenamento().maxVideoMb() * 1024 * 1024;
        AppProperties.R2 cfgR2 = props.armazenamento().r2();
        if (cfgR2 != null && cfgR2.ativo()) {
            this.r2 = new ArmazenamentoR2(cfgR2);
            log.info("Arquivos enviados ficam no Cloudflare R2, bucket {}", r2.bucket());
        } else {
            this.r2 = null;
            Files.createDirectories(raiz);
            log.info("Arquivos enviados ficam em {}", raiz);
        }
    }

    /** Salva o arquivo no disco e registra no banco. Retorna o registro. */
    public Arquivo salvar(MultipartFile arquivo, Tipo tipo) {
        if (arquivo == null || arquivo.isEmpty()) throw new RegraException("Nenhum arquivo enviado");
        Map<String, String> aceitos = tipo == Tipo.IMAGEM ? IMAGENS : VIDEOS;
        String contentType = Optional.ofNullable(arquivo.getContentType()).orElse("").toLowerCase();
        if (!aceitos.containsKey(contentType)) {
            // Alguns navegadores mandam "application/octet-stream": nesse caso olha a extensão do arquivo.
            contentType = tipoPelaExtensao(arquivo.getOriginalFilename(), aceitos);
        }
        String ext = aceitos.get(contentType);
        if (ext == null) {
            throw new RegraException(tipo == Tipo.IMAGEM
                    ? "Envie uma imagem JPG, PNG, WEBP, GIF ou AVIF"
                    : "Envie um vídeo MP4, WEBM ou MOV");
        }
        long max = tipo == Tipo.IMAGEM ? maxImagem : maxVideo;
        if (arquivo.getSize() > max) {
            throw new RegraException("Arquivo muito grande. Limite: " + (max / 1024 / 1024) + " MB");
        }

        String id = UUID.randomUUID().toString();
        LocalDate hoje = LocalDate.now();
        String relativo = "%d/%02d/%s.%s".formatted(hoje.getYear(), hoje.getMonthValue(), id, ext);
        try {
            if (r2 != null) {
                r2.gravar(relativo, arquivo.getInputStream(), arquivo.getSize(), contentType);
            } else {
                Path destino = raiz.resolve(relativo);
                Files.createDirectories(destino.getParent());
                arquivo.transferTo(destino);
            }
        } catch (IOException | RuntimeException e) {
            log.error("Falha ao salvar o arquivo {}", relativo, e);
            throw new RegraException("Não consegui salvar o arquivo. Tente de novo em instantes.");
        }
        // Se a transação der errado, apaga o arquivo para não sobrar lixo.
        aoDesfazer(() -> apagarDoDisco(relativo));

        String nome = Optional.ofNullable(arquivo.getOriginalFilename()).orElse("arquivo." + ext);
        return arquivos.save(new Arquivo(id, nome, contentType, arquivo.getSize(), relativo));
    }

    /** Remove o registro e, depois que a transação confirmar, o arquivo do disco. Ignora id nulo. */
    public void excluir(String id) {
        if (id == null) return;
        arquivos.findById(id).ifPresent(a -> {
            arquivos.delete(a);
            aoConfirmar(() -> apagarDoDisco(a.getCaminho()));
        });
    }

    private static String tipoPelaExtensao(String nome, Map<String, String> aceitos) {
        if (nome == null || !nome.contains(".")) return "";
        String ext = nome.substring(nome.lastIndexOf('.') + 1).toLowerCase();
        if (ext.equals("jpeg")) ext = "jpg";
        String procurada = ext;
        return aceitos.entrySet().stream().filter(e -> e.getValue().equals(procurada))
                .map(Map.Entry::getKey).findFirst().orElse("");
    }

    public Optional<Arquivo> buscar(String id) {
        return arquivos.findById(id);
    }

    /** True quando os arquivos estão no R2 (o controller redireciona para um link temporário). */
    public boolean usaR2() {
        return r2 != null;
    }

    public String linkR2(Arquivo a, java.time.Duration validade) {
        return r2.link(a.getCaminho(), validade);
    }

    public Path caminho(Arquivo a) {
        Path p = raiz.resolve(a.getCaminho()).normalize();
        if (!p.startsWith(raiz)) throw new IllegalStateException("Caminho inválido");
        return p;
    }

    private void apagarDoDisco(String relativo) {
        try {
            if (r2 != null) r2.apagar(relativo);
            else Files.deleteIfExists(raiz.resolve(relativo));
        } catch (IOException | RuntimeException e) {
            log.warn("Não consegui apagar {}: {}", relativo, e.getMessage());
        }
    }

    private static void aoConfirmar(Runnable r) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { r.run(); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { r.run(); }
        });
    }

    private static void aoDesfazer(Runnable r) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) { if (status != STATUS_COMMITTED) r.run(); }
        });
    }
}
