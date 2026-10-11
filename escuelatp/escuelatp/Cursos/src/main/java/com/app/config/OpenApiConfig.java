package com.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI de este microservicio.
 *
 *  - La documentacion se abre en  http://localhost:<puerto>/swagger-ui.html
 *  - Los pedidos de "Try it out" se mandan al GATEWAY (no directo al servicio),
 *    porque el servicio rechaza (403) todo lo que no pase por el Gateway.
 *  - Para probar endpoints protegidos: hacer login, copiar el token y pegarlo
 *    en el boton "Authorize".
 */
@Configuration
public class OpenApiConfig {

    @Value("${openapi.gateway-url:http://localhost:8081}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Escuela TP - Cursos y notas")
                        .version("1.0")
                        .description("Microservicio de Cursos y notas del sistema de gestion del colegio"))
                .servers(List.of(new Server().url(gatewayUrl).description("Gateway")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
