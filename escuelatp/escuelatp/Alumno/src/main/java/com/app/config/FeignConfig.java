package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

/**
 * Los microservicios rechazan (403) todo lo que no traiga la cabecera
 * X-Gateway-Secret. Cuando Alumno le pega a Cursos por Feign (sin pasar por el
 * Gateway) tiene que mandarla el mismo. El secreto es el mismo gateway.internal-secret
 * que viene del Config Server.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor gatewaySecretInterceptor(@Value("${gateway.internal-secret}") String secret) {
        return template -> template.header("X-Gateway-Secret", secret);
    }
}
