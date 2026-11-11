package br.com.felipetrainer.areamembros.conteudo;

import br.com.felipetrainer.areamembros.aluno.MatriculaRepository;
import br.com.felipetrainer.areamembros.conteudo.ConteudoDtos.*;
import br.com.felipetrainer.areamembros.midia.Arquivo;
import br.com.felipetrainer.areamembros.midia.ArmazenamentoService;
import br.com.felipetrainer.areamembros.midia.ArmazenamentoService.Tipo;
import br.com.felipetrainer.areamembros.web.NaoEncontradoException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Painel: cadastro de cursos, módulos, tópicos e vídeos, com envio e exclusão de arquivos. */
@Service
@Transactional
public class AdminConteudoService {

    private final SiteRepository sites;
    private final CursoRepository cursos;
    private final ModuloRepository modulos;
    private final TopicoRepository topicos;
    private final VideoRepository videos;
    private final MatriculaRepository matriculas;
    private final ArmazenamentoService armazenamento;
    private final Mapeador map;

    public AdminConteudoService(SiteRepository sites, CursoRepository cursos, ModuloRepository modulos,
                                TopicoRepository topicos, VideoRepository videos, MatriculaRepository matriculas,
                                ArmazenamentoService armazenamento, Mapeador map) {
        this.sites = sites;
        this.cursos = cursos;
        this.modulos = modulos;
        this.topicos = topicos;
        this.videos = videos;
        this.matriculas = matriculas;
        this.armazenamento = armazenamento;
        this.map = map;
    }

    // ---------- Site ----------

    public SiteDto atualizarSite(SiteForm f) {
        Site s = sites.findFirstByOrderByIdAsc().orElseGet(() -> sites.save(new Site()));
        s.setNome(f.nome());
        s.setSlogan(f.slogan());
        if (f.corDestaque() != null) s.setCorDestaque(f.corDestaque());
        return new SiteDto(s.getNome(), s.getSlogan(), s.getCorDestaque());
    }

    // ---------- Cursos ----------

    @Transactional(readOnly = true)
    public List<AdminCursoDto> cursos() {
        return cursos.findAllByOrderByOrdemAscIdAsc().stream().map(this::dto).toList();
    }

    public AdminCursoDto criarCurso(CursoForm f) {
        Curso c = new Curso();
        c.setSlug(Slugs.unico(f.titulo(), cursos::existsBySlug));
        c.setOrdem(f.ordem() != null ? f.ordem() : (int) cursos.count());
        aplicar(c, f);
        return dto(cursos.save(c));
    }

    public AdminCursoDto atualizarCurso(Long id, CursoForm f) {
        Curso c = curso(id);
        if (!c.getTitulo().equals(f.titulo())) c.setSlug(Slugs.unico(f.titulo(), cursos::existsBySlug));
        if (f.ordem() != null) c.setOrdem(f.ordem());
        aplicar(c, f);
        return dto(c);
    }

    public void excluirCurso(Long id) {
        Curso c = curso(id);
        List<String> arquivos = Stream.concat(Stream.of(c.getCapaId()),
                c.getModulos().stream().flatMap(this::arquivosDo)).filter(Objects::nonNull).toList();
        cursos.delete(c);
        cursos.flush();
        arquivos.forEach(armazenamento::excluir);
    }

    public AdminCursoDto enviarCapaCurso(Long id, MultipartFile arquivo) {
        Curso c = curso(id);
        Arquivo novo = armazenamento.salvar(arquivo, Tipo.IMAGEM);
        armazenamento.excluir(c.getCapaId());
        c.setCapaId(novo.getId());
        return dto(c);
    }

    public AdminCursoDto removerCapaCurso(Long id) {
        Curso c = curso(id);
        armazenamento.excluir(c.getCapaId());
        c.setCapaId(null);
        return dto(c);
    }

    // ---------- Módulos ----------

    public AdminCursoDto criarModulo(Long cursoId, ModuloForm f) {
        Curso c = curso(cursoId);
        Modulo m = c.adicionarModulo(Slugs.unico(f.titulo(), s -> modulos.existsByCursoIdAndSlug(cursoId, s)), f.titulo(), f.descricao());
        if (f.ordem() != null) m.setOrdem(f.ordem());
        if (f.publicado() != null) m.setPublicado(f.publicado());
        cursos.flush();
        return dto(c);
    }

    public AdminCursoDto atualizarModulo(Long id, ModuloForm f) {
        Modulo m = modulo(id);
        if (!m.getTitulo().equals(f.titulo())) {
            Long cursoId = m.getCurso().getId();
            m.setSlug(Slugs.unico(f.titulo(), s -> modulos.existsByCursoIdAndSlug(cursoId, s)));
        }
        m.setTitulo(f.titulo());
        m.setDescricao(f.descricao());
        if (f.ordem() != null) m.setOrdem(f.ordem());
        if (f.publicado() != null) m.setPublicado(f.publicado());
        return dto(m.getCurso());
    }

    public AdminCursoDto excluirModulo(Long id) {
        Modulo m = modulo(id);
        Curso c = m.getCurso();
        List<String> arquivos = arquivosDo(m).toList();
        c.getModulos().remove(m);
        cursos.flush();
        arquivos.forEach(armazenamento::excluir);
        return dto(c);
    }

    public AdminCursoDto enviarCapaModulo(Long id, MultipartFile arquivo) {
        Modulo m = modulo(id);
        Arquivo novo = armazenamento.salvar(arquivo, Tipo.IMAGEM);
        armazenamento.excluir(m.getCapaId());
        m.setCapaId(novo.getId());
        return dto(m.getCurso());
    }

    public AdminCursoDto removerCapaModulo(Long id) {
        Modulo m = modulo(id);
        armazenamento.excluir(m.getCapaId());
        m.setCapaId(null);
        return dto(m.getCurso());
    }

    // ---------- Tópicos ----------

    public AdminCursoDto criarTopico(Long moduloId, TopicoForm f) {
        Modulo m = modulo(moduloId);
        Topico t = m.adicionarTopico(f.titulo(), f.descricao());
        if (f.ordem() != null) t.setOrdem(f.ordem());
        modulos.flush();
        return dto(m.getCurso());
    }

    public AdminCursoDto atualizarTopico(Long id, TopicoForm f) {
        Topico t = topico(id);
        t.setTitulo(f.titulo());
        t.setDescricao(f.descricao());
        if (f.ordem() != null) t.setOrdem(f.ordem());
        return dto(t.getModulo().getCurso());
    }

    public AdminCursoDto excluirTopico(Long id) {
        Topico t = topico(id);
        Curso c = t.getModulo().getCurso();
        List<String> arquivos = t.getVideos().stream().map(Video::getArquivoId).filter(Objects::nonNull).toList();
        t.getModulo().getTopicos().remove(t);
        modulos.flush();
        arquivos.forEach(armazenamento::excluir);
        return dto(c);
    }

    // ---------- Vídeos ----------

    public AdminCursoDto criarVideo(Long topicoId, VideoForm f) {
        Topico t = topico(topicoId);
        Video v = t.adicionarVideo(f.titulo(), vazioParaNulo(f.url()));
        v.setDescricao(f.descricao());
        if (f.ordem() != null) v.setOrdem(f.ordem());
        topicos.flush();
        return dto(t.getModulo().getCurso());
    }

    public AdminCursoDto atualizarVideo(Long id, VideoForm f) {
        Video v = video(id);
        v.setTitulo(f.titulo());
        v.setDescricao(f.descricao());
        v.setUrl(vazioParaNulo(f.url()));
        if (f.ordem() != null) v.setOrdem(f.ordem());
        return dto(cursoDo(v));
    }

    public AdminCursoDto excluirVideo(Long id) {
        Video v = video(id);
        Curso c = cursoDo(v);
        String arquivo = v.getArquivoId();
        v.getTopico().getVideos().remove(v);
        topicos.flush();
        armazenamento.excluir(arquivo);
        return dto(c);
    }

    public AdminCursoDto enviarArquivoVideo(Long id, MultipartFile arquivo) {
        Video v = video(id);
        Arquivo novo = armazenamento.salvar(arquivo, Tipo.VIDEO);
        armazenamento.excluir(v.getArquivoId());
        v.setArquivoId(novo.getId());
        return dto(cursoDo(v));
    }

    public AdminCursoDto removerArquivoVideo(Long id) {
        Video v = video(id);
        armazenamento.excluir(v.getArquivoId());
        v.setArquivoId(null);
        return dto(cursoDo(v));
    }

    // ---------- Auxiliares ----------

    private AdminCursoDto dto(Curso c) {
        long alunos = c.getId() == null ? 0 : matriculas.countByCursoIdAndAtivaTrue(c.getId());
        return map.admin(c, alunos);
    }

    private Stream<String> arquivosDo(Modulo m) {
        return Stream.concat(Stream.of(m.getCapaId()),
                m.getTopicos().stream().flatMap(t -> t.getVideos().stream()).map(Video::getArquivoId))
                .filter(Objects::nonNull);
    }

    private void aplicar(Curso c, CursoForm f) {
        c.setTitulo(f.titulo());
        c.setSubtitulo(f.subtitulo());
        c.setDescricao(f.descricao());
        c.setEduzzProdutoId(vazioParaNulo(f.eduzzProdutoId()));
        c.setLinkVenda(vazioParaNulo(f.linkVenda()));
        if (f.publicado() != null) c.setPublicado(f.publicado());
    }

    private static Curso cursoDo(Video v) {
        return v.getTopico().getModulo().getCurso();
    }

    private Curso curso(Long id) {
        return cursos.findById(id).orElseThrow(() -> new NaoEncontradoException("Curso não encontrado"));
    }

    private Modulo modulo(Long id) {
        return modulos.findById(id).orElseThrow(() -> new NaoEncontradoException("Módulo não encontrado"));
    }

    private Topico topico(Long id) {
        return topicos.findById(id).orElseThrow(() -> new NaoEncontradoException("Tópico não encontrado"));
    }

    private Video video(Long id) {
        return videos.findById(id).orElseThrow(() -> new NaoEncontradoException("Vídeo não encontrado"));
    }

    private static String vazioParaNulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
