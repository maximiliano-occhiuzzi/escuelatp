package com.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Referencia, dentro del microservicio Cursos, a un alumno que vive en el
 * microservicio Alumno.
 */
@Entity
@Table(name = "alumnos")
@Data
@NoArgsConstructor
public class Alumno {

    // OJO: el id NO se genera aca. Es el id que el alumno ya tiene en el
    // microservicio Alumno (llega en el evento de RabbitMQ). Esta tabla es
    // la copia local de "que alumnos hay en cada curso".
    @Id
    private Long id;

    // @JsonIgnore + exclusiones de Lombok: Curso tiene la lista de alumnos y
    // cada Alumno apunta de vuelta a su Curso. Sin esto, convertir un Curso a
    // JSON (GET /cursos) o a texto entra en un ciclo infinito.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cursos_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Curso curso;

    public Alumno(Long id) {
        this.id = id;
    }
}
