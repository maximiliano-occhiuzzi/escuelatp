package com.app.service;

import java.util.List;

import com.app.dto.AlumnoDTO;
import com.app.mapper.IMapper;
import com.app.model.Alumno;

public interface AlumnoService {

    /** Alta: convierte con el mapper, guarda y (si trae curso) avisa a Cursos por RabbitMQ. */
    void altaAlumno(IMapper<Alumno> mapper);

    List<AlumnoDTO> listarAlumnos();

    AlumnoDTO obtenerPorId(Long id);

    /** Baja logica: pasa a DESHABILITADO, libera el curso y avisa a Cursos por RabbitMQ. */
    AlumnoDTO deshabilitarAlumno(Long id);

    AlumnoDTO habilitarAlumno(Long id);

    /** Asigna o cambia el curso de un alumno (valida curso y cupo; avisa a Cursos por RabbitMQ). */
    AlumnoDTO asignarCurso(Long id, Long cursoId);
}
