package br.com.felipetrainer.areamembros.conteudo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Objetos de entrada e saída da API de conteúdo. */
public final class ConteudoDtos {

    private ConteudoDtos() {}

    public record SiteDto(String nome, String slogan, String corDestaque) {}

    /** Card de curso na tela "Meus cursos". */
    public record CursoCardDto(Long id, String slug, String titulo, String subtitulo, String descricao,
                               String capaUrl, String linkVenda, boolean liberado, int totalModulos, int totalVideos) {}

    public record ModuloCardDto(Long id, String slug, String titulo, String descricao, String capaUrl,
                                int totalTopicos, int totalVideos) {}

    public record CursoDto(Long id, String slug, String titulo, String subtitulo, String descricao, String capaUrl,
                           String linkVenda, boolean liberado, List<ModuloCardDto> modulos) {}

    /** Vídeo. "src" é o endereço que o player usa (arquivo do servidor com link assinado, ou link externo). */
    public record VideoDto(Long id, String titulo, String descricao, int ordem, String src,
                           String url, String arquivoId, String arquivoNome, Long arquivoTamanho) {}

    public record TopicoDto(Long id, String titulo, String descricao, int ordem, List<VideoDto> videos) {}

    public record Vizinho(String slug, String titulo) {}

    public record ModuloDto(Long id, String slug, String titulo, String descricao, String capaUrl,
                            String cursoSlug, String cursoTitulo, Vizinho anterior, Vizinho proximo,
                            List<TopicoDto> topicos) {}

    // ---- Painel admin: árvore completa ----

    public record AdminModuloDto(Long id, String slug, String titulo, String descricao, String capaId, String capaUrl,
                                 int ordem, boolean publicado, List<TopicoDto> topicos) {}

    public record AdminCursoDto(Long id, String slug, String titulo, String subtitulo, String descricao,
                                String capaId, String capaUrl, String eduzzProdutoId, String linkVenda,
                                int ordem, boolean publicado, long alunos, List<AdminModuloDto> modulos) {}

    // ---- Entradas do painel admin ----

    public record SiteForm(@NotBlank @Size(max = 120) String nome,
                           @Size(max = 200) String slogan,
                           @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "use uma cor no formato #RRGGBB") String corDestaque) {}

    public record CursoForm(@NotBlank @Size(max = 160) String titulo,
                            @Size(max = 200) String subtitulo,
                            @Size(max = 2000) String descricao,
                            @Size(max = 60) String eduzzProdutoId,
                            @Size(max = 1000) String linkVenda,
                            Integer ordem,
                            Boolean publicado) {}

    public record ModuloForm(@NotBlank @Size(max = 120) String titulo,
                             @Size(max = 1000) String descricao,
                             Integer ordem,
                             Boolean publicado) {}

    public record TopicoForm(@NotBlank @Size(max = 160) String titulo,
                             @Size(max = 2000) String descricao,
                             Integer ordem) {}

    public record VideoForm(@NotBlank @Size(max = 160) String titulo,
                            @Size(max = 1000) String descricao,
                            @Size(max = 1000) String url,
                            Integer ordem) {}
}
