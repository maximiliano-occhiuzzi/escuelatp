package com.app.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Datos que se pueden cambiar de una nota (el alumno no se puede cambiar). */
public record NotaModificarDTO(
    @NotNull(message = "La nota es obligatoria")
    @DecimalMin(value = "1.0", message = "La nota minima es 1")
    @DecimalMax(value = "10.0", message = "La nota maxima es 10") Double valor,
    String descripcion,
    LocalDate fecha
) {}
