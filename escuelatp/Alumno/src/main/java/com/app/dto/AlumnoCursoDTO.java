package com.app.dto;

import jakarta.validation.constraints.NotNull;

/** Datos para asignar (o cambiar) el curso de un alumno ya existente. */
public record AlumnoCursoDTO(
    @NotNull(message = "El curso es obligatorio") Long cursoId
) {}
