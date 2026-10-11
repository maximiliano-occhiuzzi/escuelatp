package com.app.dto;

import java.time.LocalDate;

import com.app.model.Nota;

/** Lo que devuelve la API de una nota. */
public record NotaDTO(
    Long id,
    Long cursoId,
    Long alumnoId,
    Double valor,
    String descripcion,
    LocalDate fecha
) {
    public static NotaDTO from(Nota n) {
        return new NotaDTO(n.getId(), n.getCurso().getId(), n.getAlumnoId(), n.getValor(),
                n.getDescripcion(), n.getFecha());
    }
}
