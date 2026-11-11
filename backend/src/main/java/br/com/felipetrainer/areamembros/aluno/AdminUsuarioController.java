package br.com.felipetrainer.areamembros.aluno;

import br.com.felipetrainer.areamembros.auth.Sessao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Controle de usuários: cria alunos e admins, bloqueia, e libera cursos para cada um. */
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    public record UsuarioDto(Long id, String nome, String email, LocalDate dataNascimento, Papel papel, boolean ativo,
                             boolean senhaProvisoria, Usuario.Origem origem, Instant criadoEm, Set<Long> cursos) {}

    public record NovoUsuarioForm(@NotBlank String nome, @NotBlank @Email String email, LocalDate dataNascimento,
                                  @NotNull Papel papel, List<Long> cursos) {}

    public record EditarUsuarioForm(@NotBlank String nome, LocalDate dataNascimento, @NotNull Papel papel, boolean ativo) {}

    public record CursosForm(@NotNull List<Long> cursos) {}

    private final UsuarioService usuarios;
    private final Sessao sessao;

    public AdminUsuarioController(UsuarioService usuarios, Sessao sessao) {
        this.usuarios = usuarios;
        this.sessao = sessao;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return usuarios.todos().stream().map(this::dto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto criar(@Valid @RequestBody NovoUsuarioForm f) {
        Usuario u = usuarios.criar(f.nome(), f.email(), f.dataNascimento(), f.papel(), Usuario.Origem.MANUAL);
        if (f.cursos() != null && !f.cursos().isEmpty()) usuarios.definirCursos(u.getId(), f.cursos(), Usuario.Origem.MANUAL);
        return dto(u);
    }

    @PutMapping("/{id}")
    public UsuarioDto atualizar(@PathVariable Long id, @Valid @RequestBody EditarUsuarioForm f) {
        return dto(usuarios.atualizar(id, sessao.usuarioId(), f.nome(), f.dataNascimento(), f.papel(), f.ativo()));
    }

    @PutMapping("/{id}/cursos")
    public UsuarioDto cursos(@PathVariable Long id, @Valid @RequestBody CursosForm f) {
        usuarios.definirCursos(id, f.cursos(), Usuario.Origem.MANUAL);
        return listar().stream().filter(u -> u.id().equals(id)).findFirst().orElseThrow();
    }

    @PostMapping("/{id}/reenviar-acesso")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reenviar(@PathVariable Long id) {
        usuarios.reenviarAcesso(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        usuarios.excluir(id, sessao.usuarioId());
    }

    private UsuarioDto dto(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getDataNascimento(), u.getPapel(), u.isAtivo(),
                u.isSenhaProvisoria(), u.getOrigem(), u.getCriadoEm(), usuarios.cursosLiberados(u.getId()));
    }
}
