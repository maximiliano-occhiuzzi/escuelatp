package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento que Alumno publica en RabbitMQ cuando un alumno se deshabilita
 * (baja logica). Cursos lo escucha y lo saca del curso, liberando el cupo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoBajaEvent {

    private Long idAlumno;

    // curso del que se lo saca
    private Long idCurso;
}
