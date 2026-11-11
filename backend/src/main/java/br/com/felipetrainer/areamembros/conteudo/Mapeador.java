package br.com.felipetrainer.areamembros.conteudo;

import br.com.felipetrainer.areamembros.conteudo.ConteudoDtos.*;
import br.com.felipetrainer.areamembros.midia.Arquivo;
import br.com.felipetrainer.areamembros.midia.ArquivoRepository;
import br.com.felipetrainer.areamembros.midia.MidiaTokenService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Converte as entidades em DTOs (inclui os links das imagens e os links assinados dos vídeos). */
@Component
class Mapeador {

    private final MidiaTokenService tokens;
    private final ArquivoRepository arquivos;

    Mapeador(MidiaTokenService tokens, ArquivoRepository arquivos) {
        this.tokens = tokens;
        this.arquivos = arquivos;
    }

    static String img(String arquivoId) {
        return MidiaTokenService.urlImagem(arquivoId);
    }

    static int totalVideos(Modulo m) {
        return m.getTopicos().stream().mapToInt(t -> t.getVideos().size()).sum();
    }

    static List<Modulo> publicados(Curso c) {
        return c.getModulos().stream().filter(Modulo::isPublicado).toList();
    }

    CursoCardDto card(Curso c, boolean liberado) {
        List<Modulo> ms = publicados(c);
        return new CursoCardDto(c.getId(), c.getSlug(), c.getTitulo(), c.getSubtitulo(), c.getDescricao(), img(c.getCapaId()),
                c.getLinkVenda(), liberado, ms.size(), ms.stream().mapToInt(Mapeador::totalVideos).sum());
    }

    ModuloCardDto card(Modulo m) {
        return new ModuloCardDto(m.getId(), m.getSlug(), m.getTitulo(), m.getDescricao(), img(m.getCapaId()),
                m.getTopicos().size(), totalVideos(m));
    }

    List<TopicoDto> topicos(Modulo m) {
        List<String> ids = m.getTopicos().stream().flatMap(t -> t.getVideos().stream())
                .map(Video::getArquivoId).filter(Objects::nonNull).toList();
        Map<String, Arquivo> porId = arquivos.findAllById(ids).stream()
                .collect(Collectors.toMap(Arquivo::getId, Function.identity()));
        return m.getTopicos().stream().map(t -> new TopicoDto(t.getId(), t.getTitulo(), t.getDescricao(), t.getOrdem(),
                t.getVideos().stream().map(v -> video(v, porId.get(v.getArquivoId()))).toList())).toList();
    }

    private VideoDto video(Video v, Arquivo a) {
        String src = a != null ? tokens.urlVideo(a.getId()) : v.getUrl();
        return new VideoDto(v.getId(), v.getTitulo(), v.getDescricao(), v.getOrdem(), src, v.getUrl(),
                a != null ? a.getId() : null, a != null ? a.getNomeOriginal() : null, a != null ? a.getTamanho() : null);
    }

    AdminCursoDto admin(Curso c, long alunos) {
        return new AdminCursoDto(c.getId(), c.getSlug(), c.getTitulo(), c.getSubtitulo(), c.getDescricao(), c.getCapaId(),
                img(c.getCapaId()), c.getEduzzProdutoId(), c.getLinkVenda(), c.getOrdem(), c.isPublicado(), alunos,
                c.getModulos().stream().map(m -> new AdminModuloDto(m.getId(), m.getSlug(), m.getTitulo(), m.getDescricao(),
                        m.getCapaId(), img(m.getCapaId()), m.getOrdem(), m.isPublicado(), topicos(m))).toList());
    }
}
