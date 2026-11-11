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
 * Guarda imagens e vídeos numa pasta do servidor (app.armazenamento.pasta), em AAAA/MM/uuid.ext.
 * Com app.armazenamento.imagens-no-banco, as imagens ficam dentro do banco (servidor sem disco fixo).
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
    private final boolean imagensNoBanco;
    private final boolean uploadVideo;

    public ArmazenamentoService(ArquivoRepository arquivos, AppProperties props) throws IOException {
        this.arquivos = arquivos;
        this.raiz = Path.of(props.armazenamento().pasta()).toAbsolutePath().normalize();
        this.maxImagem = props.armazenamento().maxImagemMb() * 1024 * 1024;
        this.maxVideo = props.armazenamento().maxVideoMb() * 1024 * 1024;
        this.imagensNoBanco = props.armazenamento().imagensNoBanco();
        this.uploadVideo = props.armazenamento().uploadVideo();
        Files.createDirectories(raiz);
        log.info("Arquivos enviados ficam em {}{}", raiz, imagensNoBanco ? " (imagens no banco)" : "");
    }

    /** Salva o arquivo no disco e registra no banco. Retorna o registro. */
    public Arquivo salvar(MultipartFile arquivo, Tipo tipo) {
        if (arquivo == null || arquivo.isEmpty()) throw new RegraException("Nenhum arquivo enviado");
        if (tipo == Tipo.VIDEO && !uploadVideo) {
            throw new RegraException("Neste servidor os vídeos entram por link: cole o link do YouTube (não listado) ou do Vimeo");
        }
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
        String nome = Optional.ofNullable(arquivo.getOriginalFilename()).orElse("arquivo." + ext);
        if (tipo == Tipo.IMAGEM && imagensNoBanco) {
            try {
                return arquivos.save(new Arquivo(id, nome, contentType, arquivo.getSize(), "banco/" + relativo, arquivo.getBytes()));
            } catch (IOException e) {
                throw new RegraException("Não consegui ler a imagem enviada. Tente de novo.");
            }
        }
        try {
            Path destino = raiz.resolve(relativo);
            Files.createDirectories(destino.getParent());
            arquivo.transferTo(destino);
        } catch (IOException e) {
            log.error("Falha ao salvar o arquivo {}", relativo, e);
            throw new RegraException("Não consegui salvar o arquivo. Tente de novo em instantes.");
        }
        // Se a transação der errado, apaga o arquivo do disco para não sobrar lixo.
        aoDesfazer(() -> apagarDoDisco(relativo));
        return arquivos.save(new Arquivo(id, nome, contentType, arquivo.getSize(), relativo, null));
    }

    /** Remove o registro e, depois que a transação confirmar, o arquivo do disco. Ignora id nulo. */
    public void excluir(String id) {
        if (id == null) return;
        arquivos.findById(id).ifPresent(a -> {
            arquivos.delete(a);
            if (!a.isNoBanco()) aoConfirmar(() -> apagarDoDisco(a.getCaminho()));
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

    public Path caminho(Arquivo a) {
        Path p = raiz.resolve(a.getCaminho()).normalize();
        if (!p.startsWith(raiz)) throw new IllegalStateException("Caminho inválido");
        return p;
    }

    private void apagarDoDisco(String relativo) {
        try {
            Files.deleteIfExists(raiz.resolve(relativo));
        } catch (IOException e) {
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
