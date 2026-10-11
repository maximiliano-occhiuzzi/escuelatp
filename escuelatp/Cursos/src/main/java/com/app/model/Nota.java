package com.app.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Nota de un alumno en un curso. Pertenece a un Curso (relacion real de JPA)
 * y referencia al alumno por id (el alumno vive en otro microservicio).
 */
@Data
@Entity
@Table(name = "notas")
@NoArgsConstructor
public class Nota {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long alumnoId;

	@Column(nullable = false)
	private Double valor;

	// ej: "Parcial 1", "Trabajo practico"
	private String descripcion;

	private java.time.LocalDate fecha;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Curso curso;
}
