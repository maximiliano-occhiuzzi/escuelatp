package com.app.security;

import com.app.model.RefreshToken;
import com.app.model.Usuario;
import com.app.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Mucho más largo que el access token (que dura minutos): el refresh
    // token es el que sostiene la sesión "de fondo". Acá sí importa que
    // dure días, porque es el que evita tener que loguearse de nuevo
    // todo el tiempo.
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken crear(Usuario usuario) {
        RefreshToken refreshToken = new RefreshToken();
        // UUID random como valor del token: no es un JWT, es simplemente
        // un identificador opaco e imposible de adivinar. Toda la
        // "inteligencia" (quién es, cuándo vence) vive en esta tabla,
        // no en el string en sí — por eso SÍ se puede revocar.
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiraEn(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevocado(false);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Devuelve el usuario dueño del refresh token, solo si el token
     * existe, no fue revocado, y no expiró. Cualquier otro caso, vacío.
     */
    public Optional<Usuario> validarYObtenerUsuario(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevocado())
                .filter(rt -> rt.getExpiraEn().isAfter(Instant.now()))
                .map(RefreshToken::getUsuario);
    }

    /**
     * Esto es el "logout real": una vez revocado, nadie puede volver a
     * usar este refresh token para pedir un access token nuevo.
     */
    public void revocar(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                });
    }
}
