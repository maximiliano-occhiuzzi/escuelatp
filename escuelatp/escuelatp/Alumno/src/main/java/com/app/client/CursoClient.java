package com.app.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.app.dto.CursoResumenDTO;

/**
 * Cliente Feign (comunicacion SINCRONICA) hacia el microservicio Cursos.
 *
 * "modulo-cursos" es el nombre con el que Cursos se registra en Eureka: Feign
 * le pregunta al registry donde esta y balancea la carga, sin URLs fijas.
 *
 * Se usa para validar, ANTES de dar de alta un alumno con curso, que el curso
 * existe y tiene cupo. En cambio la inscripcion en si viaja por RabbitMQ
 * (asincronica): una consulta necesita respuesta inmediata, un aviso no.
 */
@FeignClient(name = "modulo-cursos")
public interface CursoClient {

    @GetMapping("/cursos/{id}")
    CursoResumenDTO obtenerCurso(@PathVariable("id") Long id);
}
