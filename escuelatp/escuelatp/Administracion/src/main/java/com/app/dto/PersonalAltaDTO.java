package com.app.dto;

import com.app.Enum.Cargo;
import com.app.mapper.IMapper;
import com.app.model.Personal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Datos para dar de alta (o modificar) un integrante del personal. */
public record PersonalAltaDTO(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") String email,
    @NotNull(message = "El cargo es obligatorio") Cargo cargo
) implements IMapper<Personal> {

    @Override
    public Personal mapperTo() {
        return new Personal(null, nombre, apellido, email, cargo);
    }
}
