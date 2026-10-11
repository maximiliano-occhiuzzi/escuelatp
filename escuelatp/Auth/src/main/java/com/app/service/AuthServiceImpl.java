package com.app.service;

import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.dto.RefreshTokenRequest;
import com.app.dto.RegisterRequest;
import com.app.exception.BadRequestException;
import com.app.exception.ConflictException;
import com.app.exception.UnauthorizedException;
import com.app.model.RefreshToken;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.repository.UsuarioRepository;
import com.app.security.JwtService;
import com.app.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Usuario o contraseña incorrectos");
        }

        return armarRespuesta(usuario);
    }

    // El refresh token es la prueba aca: no hace falta un access token vigente
    // para pedir uno nuevo (el caso tipico es justo cuando el access token YA vencio).
    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        Usuario usuario = refreshTokenService.validarYObtenerUsuario(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException(
                        "Refresh token invalido, vencido o revocado. Inicia sesion de nuevo."));

        // Rotacion: el refresh token usado queda revocado y se emite uno nuevo.
        refreshTokenService.revocar(request.getRefreshToken());

        return armarRespuesta(usuario);
    }

    // Logout "real" del lado del servidor: revoca el refresh token.
    // El ultimo access token sigue valido hasta que expira solo (por eso dura poco).
    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revocar(request.getRefreshToken());
    }

    @Override
    public void register(RegisterRequest request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("El usuario '" + request.getUsername() + "' ya existe");
        }

        if (request.getRol() == Rol.DOCENTE && request.getPersonalId() == null) {
            throw new BadRequestException(
                    "Un usuario DOCENTE tiene que estar asociado a un Personal (personalId obligatorio)");
        }

        if (request.getRol() == Rol.DOCENTE && usuarioRepository.existsByPersonalId(request.getPersonalId())) {
            throw new ConflictException("La persona del personal " + request.getPersonalId() + " ya tiene un usuario");
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setRol(request.getRol());
        // personalId solo tiene sentido para DOCENTE; en otros roles se ignora
        nuevo.setPersonalId(request.getRol() == Rol.DOCENTE ? request.getPersonalId() : null);
        try {
            usuarioRepository.saveAndFlush(nuevo);
        } catch (DataIntegrityViolationException e) {
            // Segunda barrera: dos pedidos simultaneos que pasaron la validacion de arriba.
            throw new ConflictException("El usuario ya existe o la persona del personal ya tiene un usuario");
        }
    }

    private LoginResponse armarRespuesta(Usuario usuario) {
        String accessToken = jwtService.generarToken(usuario);
        RefreshToken refreshToken = refreshTokenService.crear(usuario);

        return new LoginResponse(
                accessToken,
                "Bearer",
                refreshToken.getToken(),
                usuario.getUsername(),
                usuario.getRol().name(),
                jwtService.getExpirationMs() / 1000,
                usuario.getPersonalId()
        );
    }
}
