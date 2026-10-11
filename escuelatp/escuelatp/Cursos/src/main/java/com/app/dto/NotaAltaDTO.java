package com.app.dto;

import java.time.LocalDate;

import com.app.mapper.IMapper;
import com.app.model.Nota;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Datos para cargar una nota. El curso sale de la URL (/cursos/{cursoId}/notas). */
public record NotaAltaDTO(
    @NotNull(message = "El alumno es obligatorio") Long alumnoId,
    @NotNull(message = "La nota es obligatoria")
    @DecimalMin(value = "1.0", message = "La nota minima es 1")
    @DecimalMax(value = "10.0", message = "La nota maxima es 10") Double valor,
    String descripcion,
    // si no se manda, se usa la fecha de hoy
    LocalDate fecha
) implements IMapper<Nota> {

    // El curso lo completa el service (la nota todavia no sabe a que curso pertenece).
    @Override
    public Nota mapperTo() {
        Nota nota = new Nota();
        nota.setAlumnoId(alumnoId);
        nota.setValor(valor);
        nota.setDescripcion(descripcion);
        nota.setFecha(fecha != null ? fecha : LocalDate.now());
        return nota;
    }
}
