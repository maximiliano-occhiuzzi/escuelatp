package com.app.dto;

import com.app.model.Alumno;
import com.app.model.Estado;

/** Lo que devuelve la API de un alumno (nunca se expone la entidad directamente). */
public record AlumnoDTO(
    Long id,
    String nombre,
    String apellido,
    String dni,
    String email,
    Estado estado,
    Long cursoId
) {
    public static AlumnoDTO from(Alumno a) {
        return new AlumnoDTO(a.getId(), a.getNombre(), a.getApellido(), a.getDni(),
                a.getEmail(), a.getEstado(), a.getCursoId());
    }
}
