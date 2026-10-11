package com.app.service;

import java.util.List;

import com.app.dto.CursoDTO;
import com.app.mapper.IMapper;
import com.app.model.Curso;
import com.app.security.UsuarioActual;

public interface CursoService {

    CursoDTO altaCurso(IMapper<Curso> mapper);

    /** DOCENTE: solo sus cursos. Resto de los roles: todos. */
    List<CursoDTO> listarCursos(UsuarioActual usuario);

    /** DOCENTE: solo si el curso es suyo (si no, 403). */
    CursoDTO obtenerPorId(Long id, UsuarioActual usuario);
}
