package com.app.listener.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Copia local del evento que publica Alumno cuando un alumno se deshabilita. */
@Data
@ToString
@NoArgsConstructor
public class AlumnoBajaEvent {

    private Long idAlumno;

    private Long idCurso;
}
