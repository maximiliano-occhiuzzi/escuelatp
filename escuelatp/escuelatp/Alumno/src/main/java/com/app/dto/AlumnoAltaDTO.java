package com.app.dto;

import com.app.mapper.IMapper;
import com.app.model.Alumno;
import com.app.model.Estado;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Datos que manda el cliente para dar de alta un alumno. */
public record AlumnoAltaDTO(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank(message = "El DNI es obligatorio") String dni,
    @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") String email,
    Long cursoId
) implements IMapper<Alumno> {

    // Convierte el DTO en la entidad. Si vino con curso queda ASIGNADO,
    // si no, SINASIGNACION.
    @Override
    public Alumno mapperTo() {
        Estado estado = cursoId != null ? Estado.ASIGNADO : Estado.SINASIGNACION;
        return new Alumno(nombre, apellido, dni, email, estado, cursoId);
    }
}
