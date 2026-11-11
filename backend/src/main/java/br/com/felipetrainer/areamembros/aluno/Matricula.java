package br.com.felipetrainer.areamembros.aluno;

import br.com.felipetrainer.areamembros.conteudo.Curso;
import jakarta.persistence.*;
import java.time.Instant;

/** Liberação de um curso para um usuário. */
@Entity
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Curso curso;

    private boolean ativa = true;

    @Enumerated(EnumType.STRING)
    private Usuario.Origem origem = Usuario.Origem.MANUAL;

    private Instant criadaEm = Instant.now();

    protected Matricula() {}

    public Matricula(Usuario usuario, Curso curso, Usuario.Origem origem) {
        this.usuario = usuario;
        this.curso = curso;
        this.origem = origem;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Curso getCurso() { return curso; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public Usuario.Origem getOrigem() { return origem; }
    public Instant getCriadaEm() { return criadaEm; }
}
