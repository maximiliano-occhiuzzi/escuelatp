package com.app.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quien esta haciendo el pedido, segun las cabeceras que agrega el Gateway
 * despues de validar el JWT:
 *   X-User-Username, X-User-Roles (ej: ROLE_DOCENTE) y X-User-Personal-Id (solo DOCENTE).
 *
 * Es seguro confiar en ellas porque: (1) el Gateway borra las que mande el cliente
 * y (2) GatewayInterceptor rechaza cualquier pedido que no pase por el Gateway.
 *
 * Se usa para las reglas "de fila" que el Gateway no puede resolver solo, como
 * "el docente solo ve y modifica SUS cursos".
 */
public record UsuarioActual(String username, Set<String> roles, Long personalId) {

    public static final String USERNAME_HEADER = "X-User-Username";
    public static final String ROLES_HEADER = "X-User-Roles";
    public static final String PERSONAL_ID_HEADER = "X-User-Personal-Id";

    public static UsuarioActual desde(HttpServletRequest request) {
        String roles = request.getHeader(ROLES_HEADER);
        Set<String> rolesSet = roles == null || roles.isBlank()
                ? Set.of()
                : Arrays.stream(roles.split(",")).map(String::trim).collect(Collectors.toSet());

        Long personalId = null;
        String personalIdHeader = request.getHeader(PERSONAL_ID_HEADER);
        if (personalIdHeader != null && !personalIdHeader.isBlank()) {
            try {
                personalId = Long.valueOf(personalIdHeader.trim());
            } catch (NumberFormatException e) {
                personalId = null;
            }
        }

        return new UsuarioActual(request.getHeader(USERNAME_HEADER), rolesSet, personalId);
    }

    public boolean tieneRol(String rol) {
        return roles.contains("ROLE_" + rol);
    }

    public boolean esDocente() {
        return tieneRol("DOCENTE");
    }
}
