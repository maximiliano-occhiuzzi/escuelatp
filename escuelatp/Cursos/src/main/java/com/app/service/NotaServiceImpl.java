package com.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.NotaDTO;
import com.app.dto.NotaModificarDTO;
import com.app.exception.BadRequestException;
import com.app.exception.ForbiddenException;
import com.app.exception.ResourceNotFoundException;
import com.app.mapper.IMapper;
import com.app.model.Curso;
import com.app.model.Nota;
import com.app.repository.NotaRepository;
import com.app.security.UsuarioActual;

/**
 * Notas de un curso.
 *
 * Regla del enunciado:
 *  - El DOCENTE agrega, modifica y elimina notas SOLO en los cursos que tiene asignados.
 *  - El DIRECTOR ve todos los cursos y sus notas, pero NO puede modificarlas.
 *
 * El Gateway ya filtra por rol (que metodo puede usar cada uno). Aca se controla
 * la regla "de fila": que el docente sea el de ESTE curso. Por eso tambien se
 * vuelve a exigir el rol DOCENTE para modificar: defensa en profundidad.
 */
@Service
@Transactional
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final CursoServiceImp cursoService;

    public NotaServiceImpl(NotaRepository notaRepository, CursoServiceImp cursoService) {
        this.notaRepository = notaRepository;
        this.cursoService = cursoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaDTO> listarNotas(Long cursoId, UsuarioActual usuario) {
        Curso curso = cursoService.buscar(cursoId);
        cursoService.validarAccesoDocente(curso, usuario); // el docente solo ve las de sus cursos
        return notaRepository.findByCursoId(cursoId).stream().map(NotaDTO::from).toList();
    }

    @Override
    public NotaDTO agregarNota(Long cursoId, IMapper<Nota> mapper, UsuarioActual usuario) {
        Curso curso = cursoParaModificarNotas(cursoId, usuario);

        Nota nota = mapper.mapperTo();

        boolean alumnoInscripto = curso.getAlumnos().stream()
                .anyMatch(a -> nota.getAlumnoId().equals(a.getId()));
        if (!alumnoInscripto) {
            throw new BadRequestException("El alumno " + nota.getAlumnoId()
                    + " no esta inscripto en este curso");
        }

        curso.addNota(nota);
        return NotaDTO.from(notaRepository.save(nota));
    }

    @Override
    public NotaDTO modificarNota(Long cursoId, Long notaId, NotaModificarDTO datos, UsuarioActual usuario) {
        cursoParaModificarNotas(cursoId, usuario);
        Nota nota = buscarNota(cursoId, notaId);

        nota.setValor(datos.valor());
        nota.setDescripcion(datos.descripcion());
        nota.setFecha(datos.fecha() != null ? datos.fecha() : nota.getFecha() != null ? nota.getFecha() : LocalDate.now());

        return NotaDTO.from(notaRepository.save(nota));
    }

    @Override
    public void eliminarNota(Long cursoId, Long notaId, UsuarioActual usuario) {
        Curso curso = cursoParaModificarNotas(cursoId, usuario);
        Nota nota = buscarNota(cursoId, notaId);

        curso.getNotas().remove(nota);
        notaRepository.delete(nota);
    }

    // ---- reglas ----

    /** Busca el curso y verifica que quien pide sea el DOCENTE asignado. */
    private Curso cursoParaModificarNotas(Long cursoId, UsuarioActual usuario) {
        Curso curso = cursoService.buscar(cursoId);

        if (!usuario.esDocente()) {
            throw new ForbiddenException("Solo un docente puede agregar, modificar o eliminar notas");
        }
        if (!cursoService.esDocenteDelCurso(curso, usuario)) {
            throw new ForbiddenException("No sos el docente asignado a este curso");
        }
        return curso;
    }

    private Nota buscarNota(Long cursoId, Long notaId) {
        return notaRepository.findByIdAndCursoId(notaId, cursoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la nota " + notaId + " en el curso " + cursoId));
    }
}
