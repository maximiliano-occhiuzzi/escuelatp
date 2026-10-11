package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento que Alumno publica en RabbitMQ cuando se da de alta un alumno.
 *
 * Es un "contrato" propio, separado de la entidad Alumno: si manana cambia la
 * entidad, no se rompe el que escucha. El consumer (Cursos) tiene su propia
 * copia de esta clase, con los mismos nombres de campo (viaja como JSON).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoEvent {

    private Long idAlumno;

    private Long idCurso;
}
