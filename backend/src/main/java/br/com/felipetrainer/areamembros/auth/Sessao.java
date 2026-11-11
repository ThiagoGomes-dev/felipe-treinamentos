package br.com.felipetrainer.areamembros.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Quem está logado na requisição atual. */
@Component
public class Sessao {

    public Long usuarioId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a instanceof JwtAuthenticationToken jwt) return Long.valueOf(jwt.getToken().getSubject());
        throw new IllegalStateException("Sem usuário logado");
    }

    public boolean admin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(g -> "ROLE_ADMIN".equals(g.getAuthority()));
    }
}
