package com.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.CursoDTO;
import com.app.exception.BadRequestException;
import com.app.exception.ForbiddenException;
import com.app.exception.ResourceNotFoundException;
import com.app.mapper.IMapper;
import com.app.model.Curso;
import com.app.repository.CursoRepository;
import com.app.security.UsuarioActual;

@Service
@Transactional
public class CursoServiceImp implements CursoService {

    private final CursoRepository cursoRepository;

    public CursoServiceImp(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    public CursoDTO altaCurso(IMapper<Curso> mapper) {
        Curso curso = mapper.mapperTo();

        if (!curso.getHoraFin().isAfter(curso.getHoraInicio())) {
            throw new BadRequestException("La hora de fin tiene que ser posterior a la hora de inicio");
        }

        return CursoDTO.from(cursoRepository.save(curso));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoDTO> listarCursos(UsuarioActual usuario) {
        List<Curso> cursos;

        if (usuario.esDocente()) {
            // El docente solo ve los cursos que tiene asignados.
            cursos = usuario.personalId() == null
                    ? List.of()
                    : cursoRepository.findByDocenteId(usuario.personalId());
        } else {
            cursos = cursoRepository.findAll();
        }

        return cursos.stream().map(CursoDTO::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CursoDTO obtenerPorId(Long id, UsuarioActual usuario) {
        Curso curso = buscar(id);
        validarAccesoDocente(curso, usuario);
        return CursoDTO.from(curso);
    }

    // ---- reglas compartidas (las usa tambien NotaServiceImpl) ----

    public Curso buscar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el curso con id " + id));
    }

    /** Si quien pide es DOCENTE, el curso tiene que ser uno de los suyos. */
    public void validarAccesoDocente(Curso curso, UsuarioActual usuario) {
        if (usuario.esDocente() && !esDocenteDelCurso(curso, usuario)) {
            throw new ForbiddenException("No sos el docente asignado a este curso");
        }
    }

    public boolean esDocenteDelCurso(Curso curso, UsuarioActual usuario) {
        return usuario.personalId() != null && usuario.personalId().equals(curso.getDocenteId());
    }

    // Nota: los alumnos de un curso se cargan desde RabbitMQ (ver
    // com.app.listener.CursoListener). Alli la entidad Alumno se crea con el id
    // que ya tiene en el microservicio Alumno: es una referencia, no un alta nueva.
}
