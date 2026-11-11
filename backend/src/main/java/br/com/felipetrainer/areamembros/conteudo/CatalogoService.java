package br.com.felipetrainer.areamembros.conteudo;

import br.com.felipetrainer.areamembros.aluno.MatriculaRepository;
import br.com.felipetrainer.areamembros.auth.Sessao;
import br.com.felipetrainer.areamembros.conteudo.ConteudoDtos.*;
import br.com.felipetrainer.areamembros.web.NaoEncontradoException;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** O que o aluno logado vê: cursos (liberados ou não) e o conteúdo dos liberados. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final CursoRepository cursos;
    private final ModuloRepository modulos;
    private final SiteRepository sites;
    private final MatriculaRepository matriculas;
    private final Sessao sessao;
    private final Mapeador map;

    public CatalogoService(CursoRepository cursos, ModuloRepository modulos, SiteRepository sites,
                           MatriculaRepository matriculas, Sessao sessao, Mapeador map) {
        this.cursos = cursos;
        this.modulos = modulos;
        this.sites = sites;
        this.matriculas = matriculas;
        this.sessao = sessao;
        this.map = map;
    }

    public SiteDto site() {
        return sites.findFirstByOrderByIdAsc()
                .map(s -> new SiteDto(s.getNome(), s.getSlogan(), s.getCorDestaque()))
                .orElse(new SiteDto("Felipe Trainer", null, "#e3161b"));
    }

    /** Todos os cursos publicados; os liberados para o usuário vêm primeiro. */
    public List<CursoCardDto> cursos() {
        Set<Long> liberados = liberados();
        boolean admin = sessao.admin();
        return cursos.findByPublicadoTrueOrderByOrdemAscIdAsc().stream()
                .map(c -> map.card(c, admin || liberados.contains(c.getId())))
                .sorted((a, b) -> Boolean.compare(b.liberado(), a.liberado()))
                .toList();
    }

    public CursoDto curso(String slug) {
        Curso c = cursoPublicado(slug);
        return new CursoDto(c.getId(), c.getSlug(), c.getTitulo(), c.getSubtitulo(), c.getDescricao(),
                Mapeador.img(c.getCapaId()), c.getLinkVenda(), temAcesso(c),
                Mapeador.publicados(c).stream().map(map::card).toList());
    }

    public ModuloDto modulo(String cursoSlug, String moduloSlug) {
        Curso c = cursoPublicado(cursoSlug);
        if (!temAcesso(c)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este curso não está liberado para você");
        Modulo m = modulos.findByCursoIdAndSlug(c.getId(), moduloSlug).filter(Modulo::isPublicado)
                .orElseThrow(() -> new NaoEncontradoException("Módulo não encontrado"));

        List<Modulo> lista = Mapeador.publicados(c);
        int i = lista.indexOf(m);
        Vizinho anterior = i > 0 ? new Vizinho(lista.get(i - 1).getSlug(), lista.get(i - 1).getTitulo()) : null;
        Vizinho proximo = i >= 0 && i < lista.size() - 1 ? new Vizinho(lista.get(i + 1).getSlug(), lista.get(i + 1).getTitulo()) : null;

        return new ModuloDto(m.getId(), m.getSlug(), m.getTitulo(), m.getDescricao(), Mapeador.img(m.getCapaId()),
                c.getSlug(), c.getTitulo(), anterior, proximo, map.topicos(m));
    }

    private Curso cursoPublicado(String slug) {
        return cursos.findBySlug(slug).filter(Curso::isPublicado)
                .orElseThrow(() -> new NaoEncontradoException("Curso não encontrado"));
    }

    private boolean temAcesso(Curso c) {
        return sessao.admin() || matriculas.existsByUsuarioIdAndCursoIdAndAtivaTrue(sessao.usuarioId(), c.getId());
    }

    private Set<Long> liberados() {
        return matriculas.cursosLiberados(sessao.usuarioId());
    }
}
