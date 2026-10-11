package com.app.listener.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Copia, del lado del consumer, del evento que publica Alumno.
 * Solo tienen que coincidir los NOMBRES de los campos del JSON (no el paquete).
 */
@Data
@ToString
@NoArgsConstructor
public class AlumnoEvent {

    private Long idAlumno;

    private Long idCurso;
}
