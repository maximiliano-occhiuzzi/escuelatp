package com.app.Gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * Configuración de CORS para que el front Angular (http://localhost:4200) y
 * el Swagger de cada servicio puedan pegarle al Gateway.
 *
 * Se publica como CorsConfigurationSource y la usa Spring Security
 * (http.cors(...) en GatewaySecurityConfig). Así el filtro de CORS corre
 * ANTES de la autenticación y los errores 401/403 también llevan los headers
 * Access-Control-*. Con un CorsFilter suelto, esos errores salían sin ellos y
 * el navegador los mostraba como "Failed to fetch" en vez de 401/403.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 4200 = front Angular. 8083-8086 = Swagger UI de cada microservicio
        // (su "Try it out" le pega al Gateway desde otro puerto).
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:8083", "http://localhost:8084",
                "http://localhost:8085", "http://localhost:8086"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
