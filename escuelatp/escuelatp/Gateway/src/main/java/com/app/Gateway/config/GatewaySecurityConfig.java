package com.app.Gateway.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class GatewaySecurityConfig {

    // Mismo secret que usa Auth para firmar. Viene del application.yml
    // global del config server, compartido por todos los microservicios.
    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Lee el claim "roles" (una lista tipo ["ROLE_ADMIN"]) que armamos en
    // Auth/JwtService, y lo convierte a las authorities que entiende
    // Spring Security, para poder usar hasRole()/hasAuthority() a futuro.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix(""); // el claim ya trae el prefijo ROLE_

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // Para /auth/** el Gateway IGNORA el header Authorization (no lo valida).
    // Sin esto, si el front manda un access token ya VENCIDO a /auth/refresh o
    // /auth/login, el Gateway lo rechaza con 401 antes de llegar a Auth, justo en el
    // momento en que mas se necesita renovarlo. El header igual viaja a Auth sin
    // tocarse: /auth/register lo valida Auth por su cuenta (solo ROOT).
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
        return request -> request.getRequestURI().startsWith("/auth/") ? null : delegate.resolve(request);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder,
                                            JwtAuthenticationConverter jwtAuthenticationConverter,
                                            BearerTokenResolver bearerTokenResolver) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // el preflight de CORS (OPTIONS) tiene que pasar SIEMPRE,
                // sin JWT, o el navegador nunca ve la respuesta con los
                // headers de Access-Control-*
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // login/registro: acá es donde se CONSIGUE el token,
                // así que no puede requerir uno. Auth valida por su cuenta
                // el registro (es SU propio recurso, no el de otro micro).
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // ACÁ vive TODA la política de permisos del sistema, en
                // un solo lugar. Alumno, Cursos y Administración ya no
                // tienen SecurityConfig propio — confían en que si una
                // request les llegó, el Gateway ya la autorizó. Si mañana
                // hay que agregar/cambiar una regla, se toca UN solo
                // archivo, no N copias idénticas repartidas en cada micro.
                // ---------------- ALUMNOS ----------------
                // Alta y baja logica: solo PRECEPTOR (ROOT no puede ni agregar alumnos).
                .requestMatchers(HttpMethod.PUT, "/alumnos/add").hasRole("PRECEPTOR")
                .requestMatchers(HttpMethod.PATCH, "/alumnos/*/deshabilitar", "/alumnos/*/habilitar", "/alumnos/*/curso").hasRole("PRECEPTOR")
                // Consultar alumnos: cualquier usuario logueado (el docente necesita
                // los nombres para cargar notas).
                .requestMatchers(HttpMethod.GET, "/alumnos/**").authenticated()

                // ---------------- CURSOS ----------------
                // Crear cursos: ROOT y PRECEPTOR.
                .requestMatchers(HttpMethod.POST, "/cursos").hasAnyRole("ROOT", "PRECEPTOR")

                // ---------------- NOTAS (dentro de Cursos) ----------------
                // Ver notas: DOCENTE (solo de sus cursos, lo controla Cursos) y DIRECTOR (todas).
                .requestMatchers(HttpMethod.GET, "/cursos/*/notas/**").hasAnyRole("DOCENTE", "DIRECTOR")
                // Agregar / modificar / eliminar notas: solo DOCENTE (y solo en sus cursos).
                // El DIRECTOR las ve pero NO las puede modificar.
                .requestMatchers(HttpMethod.POST, "/cursos/*/notas/**").hasRole("DOCENTE")
                .requestMatchers(HttpMethod.PUT, "/cursos/*/notas/**").hasRole("DOCENTE")
                .requestMatchers(HttpMethod.DELETE, "/cursos/*/notas/**").hasRole("DOCENTE")

                // ---------------- PERSONAL ----------------
                // Gestionar personal: ROOT y PRECEPTOR. El DIRECTOR solo lo consulta.
                .requestMatchers(HttpMethod.POST, "/personal").hasAnyRole("ROOT", "PRECEPTOR")
                .requestMatchers(HttpMethod.PUT, "/personal/*").hasAnyRole("ROOT", "PRECEPTOR")
                .requestMatchers(HttpMethod.GET, "/personal/**").hasAnyRole("ROOT", "PRECEPTOR", "DIRECTOR")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver).jwt(jwt -> jwt
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
            ))
            // Despues de validar el JWT, agrega X-User-Username y X-User-Roles
            // a la request que se rutea a los microservicios (paso del practico).
            .addFilterAfter(new UserHeadersFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
