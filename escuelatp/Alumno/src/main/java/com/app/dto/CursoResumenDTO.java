package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Los pocos datos del curso que Alumno necesita para validar el alta
 * (el resto de los campos del curso se ignoran).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CursoResumenDTO(
    Long id,
    Integer cupoMaximo,
    Integer inscriptos
) {}
