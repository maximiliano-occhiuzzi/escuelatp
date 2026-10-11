package com.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final GatewayInterceptor gatewayInterceptor;

    public WebMvcConfig(GatewayInterceptor gatewayInterceptor) {
        this.gatewayInterceptor = gatewayInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayInterceptor)
                .addPathPatterns("/**")
                // Admin Server (y Eureka) consultan /actuator directo, sin pasar
                // por el Gateway: si se bloquea, el monitoreo se cae.
                .excludePathPatterns("/actuator/**",
                        // La documentacion Swagger se abre directo en el puerto del servicio.
                        // Solo expone la descripcion de la API, no datos. Las llamadas
                        // "Try it out" van al Gateway (ver OpenApiConfig).
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }
}
