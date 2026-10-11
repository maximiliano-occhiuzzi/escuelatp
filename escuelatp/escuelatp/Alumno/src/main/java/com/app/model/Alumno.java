package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alumnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private String dni;
    private String email;

    // Se guarda como texto (ASIGNADO, SINASIGNACION...) y no como numero,
    // asi la base se entiende sola y no se rompe si se reordena el enum.
    // Los alumnos NUNCA se borran: se pasan a DESHABILITADO (baja logica).
    @Enumerated(EnumType.STRING)
    private Estado estado;

    // Id del curso al que esta inscripto. Es solo el id (el curso vive en otro
    // microservicio, con otra base): no hay FK ni relacion JPA entre ellos.
    private Long cursoId;

    public Alumno(String nombre, String apellido, String dni, String email, Estado estado, Long cursoId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.estado = estado;
        this.cursoId = cursoId;
    }
}
