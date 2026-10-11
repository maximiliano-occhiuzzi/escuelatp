package com.app.dto;

import com.app.Enum.Cargo;
import com.app.model.Personal;

/** Lo que devuelve la API de un integrante del personal. */
public record PersonalDTO(
    Long id,
    String nombre,
    String apellido,
    String email,
    Cargo cargo
) {
    public static PersonalDTO from(Personal p) {
        return new PersonalDTO(p.getId(), p.getNombre(), p.getApellido(), p.getEmail(), p.getCargo());
    }
}
