package com.app.dto;

import com.app.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    // ROOT, DIRECTOR, PRECEPTOR o DOCENTE
    @NotNull
    private Rol rol;

    // Obligatorio solo si el rol es DOCENTE: id del Personal asociado.
    private Long personalId;
}
