package br.com.felipetrainer.areamembros.aluno;

import br.com.felipetrainer.areamembros.conteudo.Curso;
import br.com.felipetrainer.areamembros.conteudo.CursoRepository;
import br.com.felipetrainer.areamembros.email.EmailService;
import br.com.felipetrainer.areamembros.web.NaoEncontradoException;
import br.com.felipetrainer.areamembros.web.RegraException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Usuários (alunos e admins) e liberação de cursos. */
@Service
@Transactional
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarios;
    private final MatriculaRepository matriculas;
    private final CompraRepository compras;
    private final CursoRepository cursos;
    private final PasswordEncoder encoder;
    private final EmailService email;

    public UsuarioService(UsuarioRepository usuarios, MatriculaRepository matriculas, CompraRepository compras,
                          CursoRepository cursos, PasswordEncoder encoder, EmailService email) {
        this.usuarios = usuarios;
        this.matriculas = matriculas;
        this.compras = compras;
        this.cursos = cursos;
        this.encoder = encoder;
        this.email = email;
    }

    // ---------- Cadastro ----------

    /** Cria o usuário com senha provisória e manda o e-mail de acesso. */
    public Usuario criar(String nome, String emailUsuario, LocalDate nascimento, Papel papel, Usuario.Origem origem) {
        String e = normalizar(emailUsuario);
        if (usuarios.findByEmailIgnoreCase(e).isPresent()) throw new RegraException("Já existe um usuário com esse e-mail");
        Usuario u = new Usuario();
        u.setNome(nome == null || nome.isBlank() ? e : nome.trim());
        u.setEmail(e);
        u.setDataNascimento(nascimento);
        u.setPapel(papel);
        u.setOrigem(origem);
        String senha = SenhaProvisoria.para(nascimento);
        u.setSenhaHash(encoder.encode(senha));
        u.setSenhaProvisoria(true);
        usuarios.save(u);
        email.enviarBoasVindas(u, senha);
        log.info("Usuário criado: {} ({}, origem {})", e, papel, origem);
        return u;
    }

    public Usuario atualizar(Long id, Long quemEdita, String nome, LocalDate nascimento, Papel papel, boolean ativo) {
        Usuario u = usuario(id);
        if (id.equals(quemEdita) && (papel != Papel.ADMIN || !ativo)) {
            throw new RegraException("Você não pode tirar o seu próprio acesso de admin nem se bloquear");
        }
        if (u.getPapel() == Papel.ADMIN && (papel != Papel.ADMIN || !ativo) && usuarios.countByPapelAndAtivoTrue(Papel.ADMIN) <= 1) {
            throw new RegraException("Precisa existir pelo menos um admin ativo");
        }
        u.setNome(nome.trim());
        u.setDataNascimento(nascimento);
        u.setPapel(papel);
        u.setAtivo(ativo);
        return u;
    }

    public void excluir(Long id, Long quemEdita) {
        Usuario u = usuario(id);
        if (id.equals(quemEdita)) throw new RegraException("Você não pode excluir o próprio usuário");
        if (u.getPapel() == Papel.ADMIN && usuarios.countByPapelAndAtivoTrue(Papel.ADMIN) <= 1) {
            throw new RegraException("Precisa existir pelo menos um admin ativo");
        }
        usuarios.delete(u);
    }

    /** Volta a senha para a provisória e reenvia o e-mail de acesso. */
    public void reenviarAcesso(Long id) {
        Usuario u = usuario(id);
        String senha = SenhaProvisoria.para(u.getDataNascimento());
        u.setSenhaHash(encoder.encode(senha));
        u.setSenhaProvisoria(true);
        email.enviarBoasVindas(u, senha);
    }

    @Transactional(readOnly = true)
    public List<Usuario> todos() {
        return usuarios.findAllByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public Set<Long> cursosLiberados(Long usuarioId) {
        return matriculas.cursosLiberados(usuarioId);
    }

    // ---------- Cursos liberados ----------

    /** Deixa liberados exatamente os cursos informados (o resto é bloqueado). */
    public void definirCursos(Long usuarioId, Collection<Long> cursoIds, Usuario.Origem origem) {
        Usuario u = usuario(usuarioId);
        Set<Long> desejados = new HashSet<>(cursoIds);
        for (Matricula m : matriculas.findByUsuarioId(usuarioId)) {
            m.setAtiva(desejados.remove(m.getCurso().getId()));
        }
        for (Long cursoId : desejados) {
            Curso c = cursos.findById(cursoId).orElseThrow(() -> new NaoEncontradoException("Curso não encontrado"));
            matriculas.save(new Matricula(u, c, origem));
        }
    }

    /** Libera um curso. Devolve true se o curso não estava liberado antes. */
    public boolean liberarCurso(Usuario u, Curso c, Usuario.Origem origem) {
        var existente = matriculas.findByUsuarioIdAndCursoId(u.getId(), c.getId());
        if (existente.isPresent()) {
            boolean estava = existente.get().isAtiva();
            existente.get().setAtiva(true);
            return !estava;
        }
        matriculas.save(new Matricula(u, c, origem));
        return true;
    }

    // ---------- Eduzz ----------

    /** Aviso de compra: cria o aluno se precisar e libera (ou bloqueia, no reembolso) o curso do produto. */
    public void processarCompra(String transacao, String status, boolean aprovada, boolean cancelada,
                                String produtoId, String nome, String emailAluno, LocalDate nascimento) {
        Compra compra = compras.findByTransacao(transacao).orElse(null);
        if (aprovada) {
            Curso curso = cursoDoProduto(produtoId);
            Usuario u = usuarios.findByEmailIgnoreCase(normalizar(emailAluno)).orElse(null);
            boolean novo = u == null;
            if (novo) u = criar(nome, emailAluno, nascimento, Papel.ALUNO, Usuario.Origem.EDUZZ);
            if (!u.isAtivo()) u.setAtivo(true);
            if (curso != null && liberarCurso(u, curso, Usuario.Origem.EDUZZ) && !novo) {
                email.enviarCursoLiberado(u, curso.getTitulo());
            }
            if (compra == null) {
                compra = new Compra();
                compra.setTransacao(transacao);
                compra.setUsuario(u);
            }
            compra.setCurso(curso);
            compra.setStatus(status);
            compras.save(compra);
        } else if (cancelada && compra != null) {
            compra.setStatus(status);
            if (compra.getCurso() != null) {
                matriculas.findByUsuarioIdAndCursoId(compra.getUsuario().getId(), compra.getCurso().getId())
                        .ifPresent(m -> m.setAtiva(false));
            }
            log.info("Curso bloqueado para {} (status {})", compra.getUsuario().getEmail(), status);
        } else if (compra != null) {
            compra.setStatus(status);
        }
    }

    /** Curso ligado ao produto da Eduzz. Se só existir um curso, usa ele. */
    private Curso cursoDoProduto(String produtoId) {
        if (produtoId != null) {
            var c = cursos.findFirstByEduzzProdutoId(produtoId);
            if (c.isPresent()) return c.get();
        }
        List<Curso> todos = cursos.findAll();
        if (todos.size() == 1) return todos.getFirst();
        log.warn("Compra sem curso correspondente (produto {}). Libere o curso manualmente no painel.", produtoId);
        return null;
    }

    private Usuario usuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
    }

    private static String normalizar(String e) {
        return e.trim().toLowerCase();
    }
}
