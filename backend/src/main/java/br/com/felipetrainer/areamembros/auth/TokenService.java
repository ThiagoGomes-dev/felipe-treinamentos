package br.com.felipetrainer.areamembros.auth;

import br.com.felipetrainer.areamembros.aluno.Usuario;
import br.com.felipetrainer.areamembros.config.AppProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final AppProperties props;

    public TokenService(JwtEncoder encoder, AppProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public String gerar(Usuario u) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("area-membros")
                .subject(String.valueOf(u.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plus(Duration.ofHours(props.jwt().horasValidade())))
                .claim("email", u.getEmail())
                .claim("roles", List.of(u.getPapel().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
