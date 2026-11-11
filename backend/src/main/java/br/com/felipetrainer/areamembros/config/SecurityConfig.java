package br.com.felipetrainer.areamembros.config;

import br.com.felipetrainer.areamembros.aluno.Usuario;
import br.com.felipetrainer.areamembros.aluno.UsuarioRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, UsuarioRepository usuarios, AppProperties props) throws Exception {
        http
            .cors(c -> c.configurationSource(cors(props)))
            .csrf(csrf -> csrf.disable()) // API sem cookies: o token vai no header Authorization
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/site", "/api/midia/**").permitAll() // vídeos exigem link assinado
                .requestMatchers("/api/webhooks/**").permitAll() // protegido pelo token da Eduzz
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()) // arquivos do frontend
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(conversor(usuarios))))
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable());
        return http.build();
    }

    /**
     * Transforma o token em usuário logado e confere no banco se o acesso continua ativo
     * (assim, um reembolso bloqueia o aluno na hora, sem esperar o token vencer).
     */
    private Converter<Jwt, AbstractAuthenticationToken> conversor(UsuarioRepository usuarios) {
        return jwt -> {
            Usuario u = usuarios.findById(Long.valueOf(jwt.getSubject()))
                    .filter(Usuario::isAtivo)
                    .orElseThrow(() -> new InvalidBearerTokenException("Acesso bloqueado ou inexistente"));
            return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + u.getPapel().name())),
                    u.getEmail());
        };
    }

    /**
     * Libera o frontend hospedado em outro domínio (ex.: Vercel) para chamar a API.
     * Origens em CORS_ORIGINS, separadas por vírgula. Aceita curinga: https://*.vercel.app
     */
    private static CorsConfigurationSource cors(AppProperties props) {
        CorsConfiguration c = new CorsConfiguration();
        String origens = props.corsOrigens() == null ? "" : props.corsOrigens();
        c.setAllowedOriginPatterns(Arrays.stream(origens.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", c);
        return fonte;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder(AppProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave(props)));
    }

    @Bean
    JwtDecoder jwtDecoder(AppProperties props) {
        return NimbusJwtDecoder.withSecretKey(chave(props)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static SecretKey chave(AppProperties props) {
        byte[] bytes = props.jwt().segredo().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.segredo (JWT_SECRET) precisa ter pelo menos 32 caracteres");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
