package com.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Viene del config server, del application.yml GLOBAL (compartido por
    // todos los microservicios). Tiene que ser el MISMO valor en todos lados:
    // el que emite acá y el que valida en Gateway/Alumnos/Cursos/Administracion.
    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Para EMITIR tokens (usado por JwtService en /auth/login).
    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    // Para VALIDAR tokens. Auth ahora también necesita esto, porque
    // /auth/register va a exigir que quien lo llame ya venga con un JWT
    // válido y rol ROOT — ya no es un endpoint 100% abierto.
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Mismo converter que en el Gateway y en los microservicios de dominio:
    // lee el claim "roles" en vez del "scope" por default.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // login / refresh / logout NO usan el access token como credencial: si el front
    // manda uno vencido junto con el refresh token, no hay que rechazarlo con 401.
    // Solo /auth/register (y el resto) leen el Authorization.
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
        return request -> {
            String uri = request.getRequestURI();
            boolean publico = uri.equals("/auth/login") || uri.equals("/auth/refresh") || uri.equals("/auth/logout");
            return publico ? null : delegate.resolve(request);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder,
                                            JwtAuthenticationConverter jwtAuthenticationConverter,
                                            BearerTokenResolver bearerTokenResolver) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // login sigue abierto: para pedir un token todavía no tenés uno
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                // refresh y logout usan el refresh token como credencial
                // propia (no el access token) — no tiene sentido exigir
                // un JWT vigente para pedir uno nuevo, o para cerrar sesión
                .requestMatchers(HttpMethod.POST, "/auth/refresh", "/auth/logout").permitAll()
                .requestMatchers("/actuator/**", "/h2-console/**").permitAll()
                // documentacion Swagger / OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // registrar usuarios nuevos: solo alguien YA logueado como ROOT
                .requestMatchers(HttpMethod.POST, "/auth/register").hasRole("ROOT")
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // para el h2-console
            .oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver).jwt(jwt -> jwt
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
            ));

        return http.build();
    }
}
