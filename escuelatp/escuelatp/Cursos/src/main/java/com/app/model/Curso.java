package com.app.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.app.enums.DiaSemana;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Un curso = una materia dictada en un dia y horario, con un cupo y un docente.
 *
 * Relaciones:
 *  - Curso 1 --- N Alumno : los alumnos inscriptos (referencia local por id al
 *    microservicio Alumno, se carga desde RabbitMQ).
 *  - Curso 1 --- N Nota   : las notas cargadas por el docente en este curso.
 *  - docenteId: id del Personal (microservicio Administracion). Es solo el id
 *    porque esa entidad vive en otro microservicio con otra base de datos.
 */
@Data
@Entity
@Table(name = "cursos")
@NoArgsConstructor
public class Curso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String materia;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiaSemana diaSemana;

	@Column(nullable = false)
	private LocalTime horaInicio;

	@Column(nullable = false)
	private LocalTime horaFin;

	@Column(nullable = false)
	private Integer cupoMaximo;

	@Column(nullable = false)
	private Integer cicloLectivo;

	// Docente asignado (id de Personal en Administracion)
	private Long docenteId;

	// Las colecciones se excluyen de equals/hashCode/toString para evitar
	// ciclos y cargas perezosas innecesarias.
	@OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Alumno> alumnos = new ArrayList<>();

	@OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Nota> notas = new ArrayList<>();

	public void addAlumno(Alumno alumno) {
		alumnos.add(alumno);
		alumno.setCurso(this);
	}

	public void removeAlumno(Alumno alumno) {
		alumnos.remove(alumno);
		alumno.setCurso(null);
	}

	public void addNota(Nota nota) {
		notas.add(nota);
		nota.setCurso(this);
	}

	public void removeNota(Nota nota) {
		notas.remove(nota);
		nota.setCurso(null);
	}
}
