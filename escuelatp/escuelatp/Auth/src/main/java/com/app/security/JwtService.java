package com.app.security;

import com.app.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("escuelatp-auth")
                .issuedAt(ahora)
                .expiresAt(ahora.plusMillis(expirationMs))
                // el "subject" es el username: es lo que cada microservicio
                // va a leer como "quien hizo esta request"
                .subject(usuario.getUsername())
                // claim custom con el rol, para poder hacer autorizacion
                // por rol (ej: solo PRECEPTOR puede dar de alta alumnos)
                .claim("roles", List.of("ROLE_" + usuario.getRol().name()));

        // Solo los DOCENTE tienen personalId. El builder no acepta valores null.
        if (usuario.getPersonalId() != null) {
            claims.claim("personalId", usuario.getPersonalId());
        }

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims.build())).getTokenValue();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
