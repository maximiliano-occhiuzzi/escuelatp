package com.app.config;

import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Usuarios de prueba, uno por rol, solo si la base esta vacia.
 *
 * El DOCENTE queda asociado al Personal con id 1, que Administracion crea al
 * arrancar (ver su DataInitializer). Eso permite probar todo sin cargar nada a mano.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        crear("root", "root123", Rol.ROOT, null);
        crear("director", "director123", Rol.DIRECTOR, null);
        crear("preceptor", "preceptor123", Rol.PRECEPTOR, null);
        crear("docente", "docente123", Rol.DOCENTE, 1L);

        log.info("Usuarios de prueba creados: root/root123, director/director123, "
                + "preceptor/preceptor123, docente/docente123");
    }

    private void crear(String username, String password, Rol rol, Long personalId) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setPersonalId(personalId);
        usuarioRepository.save(u);
    }
}
