package com.app.dto;

import java.time.LocalTime;

import com.app.enums.DiaSemana;
import com.app.mapper.IMapper;
import com.app.model.Curso;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Datos para crear un curso. */
public record CursoAltaDTO(
    @NotBlank(message = "La materia es obligatoria") String materia,
    @NotNull(message = "El dia es obligatorio") DiaSemana diaSemana,
    @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
    @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
    @NotNull(message = "El cupo maximo es obligatorio") @Min(value = 1, message = "El cupo minimo es 1") Integer cupoMaximo,
    @NotNull(message = "El ciclo lectivo es obligatorio") @Min(2000) @Max(2100) Integer cicloLectivo,
    // id del Personal (DOCENTE) que dicta el curso; opcional al crear
    Long docenteId
) implements IMapper<Curso> {

    @Override
    public Curso mapperTo() {
        Curso curso = new Curso();
        curso.setMateria(materia);
        curso.setDiaSemana(diaSemana);
        curso.setHoraInicio(horaInicio);
        curso.setHoraFin(horaFin);
        curso.setCupoMaximo(cupoMaximo);
        curso.setCicloLectivo(cicloLectivo);
        curso.setDocenteId(docenteId);
        return curso;
    }
}
