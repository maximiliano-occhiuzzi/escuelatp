package com.app.dto;

import java.time.LocalTime;
import java.util.List;

import com.app.enums.DiaSemana;
import com.app.model.Alumno;
import com.app.model.Curso;

/** Lo que devuelve la API de un curso. */
public record CursoDTO(
    Long id,
    String materia,
    DiaSemana diaSemana,
    LocalTime horaInicio,
    LocalTime horaFin,
    Integer cupoMaximo,
    Integer cicloLectivo,
    Long docenteId,
    int inscriptos,
    List<Long> alumnosIds
) {
    public static CursoDTO from(Curso c) {
        List<Long> ids = c.getAlumnos().stream().map(Alumno::getId).toList();
        return new CursoDTO(c.getId(), c.getMateria(), c.getDiaSemana(), c.getHoraInicio(), c.getHoraFin(),
                c.getCupoMaximo(), c.getCicloLectivo(), c.getDocenteId(), ids.size(), ids);
    }
}
