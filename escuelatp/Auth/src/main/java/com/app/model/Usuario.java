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
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // Nunca se guarda en texto plano: siempre pasa por BCryptPasswordEncoder
    // antes de llegar aca (ver AuthServiceImpl y DataInitializer).
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    // Solo para usuarios DOCENTE: id del registro de Personal (microservicio
    // Administracion) al que corresponde este usuario. Es solo el id, no hay
    // relacion JPA porque Personal vive en otro microservicio con otra base.
    // Viaja en el JWT y es lo que permite saber "cuales son MIS cursos".
    // unique: una persona del personal tiene como maximo un usuario (varios NULL
    // si estan permitidos: los usuarios que no son DOCENTE no tienen personalId).
    @Column(unique = true)
    private Long personalId;
}
