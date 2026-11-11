package br.com.felipetrainer.areamembros.auth;

import br.com.felipetrainer.areamembros.aluno.Papel;
import br.com.felipetrainer.areamembros.aluno.Usuario;
import br.com.felipetrainer.areamembros.aluno.UsuarioRepository;
import br.com.felipetrainer.areamembros.web.RegraException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginForm(@NotBlank String email, @NotBlank String senha) {}

    public record TrocarSenhaForm(@NotBlank String senhaAtual,
                                  @NotBlank @Size(min = 6, message = "a nova senha precisa ter pelo menos 6 caracteres") String novaSenha) {}

    public record UsuarioDto(Long id, String nome, String email, Papel papel, boolean senhaProvisoria) {
        static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getPapel(), u.isSenhaProvisoria());
        }
    }

    public record LoginResposta(String token, UsuarioDto usuario) {}

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public LoginResposta login(@Valid @RequestBody LoginForm form) {
        Usuario u = usuarios.findByEmailIgnoreCase(form.email().trim())
                .filter(x -> encoder.matches(form.senha(), x.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));
        if (!u.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu acesso está bloqueado. Fale com o suporte.");
        }
        return new LoginResposta(tokens.gerar(u), UsuarioDto.de(u));
    }

    @GetMapping("/me")
    public UsuarioDto eu(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioDto.de(usuarioDo(jwt));
    }

    @PostMapping("/trocar-senha")
    @Transactional
    public UsuarioDto trocarSenha(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TrocarSenhaForm form) {
        Usuario u = usuarioDo(jwt);
        if (!encoder.matches(form.senhaAtual(), u.getSenhaHash())) {
            throw new RegraException("A senha atual está incorreta");
        }
        if (form.senhaAtual().equals(form.novaSenha())) {
            throw new RegraException("A nova senha precisa ser diferente da atual");
        }
        u.setSenhaHash(encoder.encode(form.novaSenha()));
        u.setSenhaProvisoria(false);
        return UsuarioDto.de(u);
    }

    private Usuario usuarioDo(Jwt jwt) {
        return usuarios.findById(Long.valueOf(jwt.getSubject()))
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
