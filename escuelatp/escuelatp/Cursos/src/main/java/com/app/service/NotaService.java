package com.app.service;

import java.util.List;

import com.app.dto.NotaDTO;
import com.app.dto.NotaModificarDTO;
import com.app.mapper.IMapper;
import com.app.model.Nota;
import com.app.security.UsuarioActual;

public interface NotaService {

    /** DOCENTE: solo de sus cursos. DIRECTOR: de todos (solo lectura). */
    List<NotaDTO> listarNotas(Long cursoId, UsuarioActual usuario);

    /** Solo el DOCENTE asignado al curso. El alumno tiene que estar inscripto. */
    NotaDTO agregarNota(Long cursoId, IMapper<Nota> mapper, UsuarioActual usuario);

    /** Solo el DOCENTE asignado al curso. */
    NotaDTO modificarNota(Long cursoId, Long notaId, NotaModificarDTO datos, UsuarioActual usuario);

    /** Solo el DOCENTE asignado al curso. */
    void eliminarNota(Long cursoId, Long notaId, UsuarioActual usuario);
}
