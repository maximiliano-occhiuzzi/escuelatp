package com.app.Gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Equivalente Gateway MVC del "request.mutate().header(...)" del practico.
 *
 * Corre DESPUES de que Spring Security validó el JWT. Le agrega a la request
 * que se va a rutear a los microservicios:
 *   X-User-Username -> subject del JWT
 *   X-User-Roles    -> claim "roles" (ej: ROLE_DOCENTE), separado por comas
 *   X-User-Personal-Id -> claim "personalId" (solo usuarios DOCENTE)
 *
 * Seguridad: antes de agregar las cabeceras propias, borra cualquier
 * X-User-* que haya mandado el cliente, para que nadie pueda hacerse pasar
 * por otro usuario mandando esas cabeceras a mano.
 *
 * (El X-Gateway-Secret lo agrega el filtro SetRequestHeader de las rutas en
 * el yml del repo de properties.)
 *
 * No es un @Component a proposito: se registra solo en la SecurityFilterChain
 * (GatewaySecurityConfig) para que no se ejecute dos veces.
 */
public class UserHeadersFilter extends OncePerRequestFilter {

    public static final String USERNAME_HEADER = "X-User-Username";
    public static final String ROLES_HEADER = "X-User-Roles";
    // Solo para DOCENTE: id del Personal asociado (claim "personalId" del JWT)
    public static final String PERSONAL_ID_HEADER = "X-User-Personal-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Map<String, String> added = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            added.put(USERNAME_HEADER, jwt.getSubject());

            List<String> roles = jwt.getClaimAsStringList("roles");
            added.put(ROLES_HEADER, roles == null ? "" : String.join(",", roles));

            Object personalId = jwt.getClaim("personalId");
            if (personalId != null) {
                added.put(PERSONAL_ID_HEADER, String.valueOf(personalId));
            }
        }

        chain.doFilter(new UserHeadersRequestWrapper(request, added), response);
    }

    /** Request que oculta los X-User-* del cliente y expone los del Gateway. */
    private static class UserHeadersRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> added;

        UserHeadersRequestWrapper(HttpServletRequest request, Map<String, String> added) {
            super(request);
            this.added = added;
        }

        private boolean isUserHeader(String name) {
            return USERNAME_HEADER.equalsIgnoreCase(name)
                    || ROLES_HEADER.equalsIgnoreCase(name)
                    || PERSONAL_ID_HEADER.equalsIgnoreCase(name);
        }

        @Override
        public String getHeader(String name) {
            if (isUserHeader(name)) {
                return added.get(name);
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isUserHeader(name)) {
                String value = added.get(name);
                return value == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Map<String, Boolean> names = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Enumeration<String> original = super.getHeaderNames();
            while (original != null && original.hasMoreElements()) {
                String n = original.nextElement();
                if (!isUserHeader(n)) {
                    names.put(n, Boolean.TRUE);
                }
            }
            added.keySet().forEach(n -> names.put(n, Boolean.TRUE));
            return Collections.enumeration(names.keySet());
        }
    }
}
