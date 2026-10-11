package com.app.service;

import com.app.client.CursoClient;
import com.app.config.RabbitMQConfig;
import com.app.dto.AlumnoDTO;
import com.app.dto.CursoResumenDTO;
import com.app.exception.BadRequestException;
import com.app.exception.ConflictException;
import com.app.exception.ResourceNotFoundException;
import com.app.exception.ServiceUnavailableException;
import com.app.mapper.IMapper;
import com.app.model.Alumno;
import com.app.model.Estado;
import com.app.repository.AlumnoRepository;
import com.app.senders.AlumnoBajaEvent;
import com.app.senders.AlumnoEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CursoClient cursoClient;

    @Override
    @Transactional
    public void altaAlumno(IMapper<Alumno> mapper) {
        Alumno nuevoAlumno = mapper.mapperTo();

        if (alumnoRepository.existsByDni(nuevoAlumno.getDni())) {
            throw new ConflictException("Ya existe un alumno con el DNI " + nuevoAlumno.getDni());
        }

        // Si viene con curso, se valida (Feign, sincronico) que exista y tenga cupo
        // ANTES de guardar: asi nunca queda un alumno ASIGNADO a un curso que no lo puede recibir.
        if (nuevoAlumno.getCursoId() != null) {
            validarCurso(nuevoAlumno.getCursoId());
        }

        nuevoAlumno = alumnoRepository.save(nuevoAlumno);

        // Si el alumno no vino con curso no hay nada que avisarle a Cursos.
        if (nuevoAlumno.getCursoId() != null) {
            AlumnoEvent event = new AlumnoEvent(nuevoAlumno.getId(), nuevoAlumno.getCursoId());
            publicar(RabbitMQConfig.ROUTING_KEY, event, nuevoAlumno.getId());
        }
    }

    @Override
    public List<AlumnoDTO> listarAlumnos() {
        return alumnoRepository.findAll().stream().map(AlumnoDTO::from).toList();
    }

    @Override
    public AlumnoDTO obtenerPorId(Long id) {
        return AlumnoDTO.from(buscar(id));
    }

    @Override
    @Transactional
    public AlumnoDTO deshabilitarAlumno(Long id) {
        Alumno alumno = buscar(id);

        if (alumno.getEstado() == Estado.DESHABILITADO) {
            throw new ConflictException("El alumno " + id + " ya esta deshabilitado");
        }

        Long cursoAnterior = alumno.getCursoId();

        // Baja logica: el registro se conserva, solo cambia el estado.
        alumno.setEstado(Estado.DESHABILITADO);
        alumno.setCursoId(null);
        alumno = alumnoRepository.save(alumno);

        // Si estaba en un curso, Cursos tiene que sacarlo de su lista (libera el cupo).
        if (cursoAnterior != null) {
            publicar(RabbitMQConfig.BAJA_ROUTING_KEY, new AlumnoBajaEvent(alumno.getId(), cursoAnterior), alumno.getId());
        }

        return AlumnoDTO.from(alumno);
    }

    // Reactiva un alumno deshabilitado. Vuelve SIN curso (SINASIGNACION): al
    // deshabilitarlo ya se lo sacó del curso, así que no hay que avisar a Cursos.
    @Override
    @Transactional
    public AlumnoDTO habilitarAlumno(Long id) {
        Alumno alumno = buscar(id);

        if (alumno.getEstado() != Estado.DESHABILITADO) {
            throw new ConflictException("El alumno " + id + " no esta deshabilitado");
        }

        alumno.setEstado(Estado.SINASIGNACION);
        alumno.setCursoId(null);
        return AlumnoDTO.from(alumnoRepository.save(alumno));
    }

    // Asigna un curso a un alumno que no tiene, o lo cambia de curso.
    @Override
    @Transactional
    public AlumnoDTO asignarCurso(Long id, Long cursoId) {
        Alumno alumno = buscar(id);

        if (alumno.getEstado() == Estado.DESHABILITADO) {
            throw new ConflictException("El alumno " + id + " esta deshabilitado: habilitalo antes de asignarle un curso");
        }
        if (cursoId.equals(alumno.getCursoId())) {
            throw new ConflictException("El alumno " + id + " ya esta en el curso " + cursoId);
        }

        // Igual que en el alta: Feign (sincronico) valida que exista y tenga cupo.
        validarCurso(cursoId);

        Long cursoAnterior = alumno.getCursoId();
        alumno.setCursoId(cursoId);
        alumno.setEstado(Estado.ASIGNADO);
        alumno = alumnoRepository.save(alumno);

        // Si cambia de curso, Cursos lo saca del anterior (libera cupo) y lo inscribe en el nuevo.
        if (cursoAnterior != null) {
            publicar(RabbitMQConfig.BAJA_ROUTING_KEY, new AlumnoBajaEvent(alumno.getId(), cursoAnterior), alumno.getId());
        }
        publicar(RabbitMQConfig.ROUTING_KEY, new AlumnoEvent(alumno.getId(), cursoId), alumno.getId());

        return AlumnoDTO.from(alumno);
    }

    private Alumno buscar(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el alumno con id " + id));
    }

    private void validarCurso(Long cursoId) {
        CursoResumenDTO curso;
        try {
            curso = cursoClient.obtenerCurso(cursoId);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("El curso " + cursoId + " no existe");
        } catch (FeignException e) {
            // Cursos caido, sin respuesta, o respondio un error inesperado
            throw new ServiceUnavailableException(
                    "No se pudo verificar el curso " + cursoId + " (el microservicio Cursos no responde). Intenta de nuevo.");
        }

        if (curso.cupoMaximo() != null && curso.inscriptos() != null
                && curso.inscriptos() >= curso.cupoMaximo()) {
            throw new ConflictException("El curso " + cursoId + " no tiene cupo disponible");
        }
    }

    // El cambio en la base ya se hizo. Publicar el evento es "nice to have": si
    // RabbitMQ no esta levantado, la operacion igual tiene que poder completarse.
    // Se loguea y se sigue, en vez de romper toda la request.
    //
    // El mensaje se manda DESPUES del commit: si la transaccion fallara y se
    // deshiciera, no tiene que haber salido un aviso de algo que no ocurrio.
    private void publicar(String routingKey, Object evento, Long idAlumno) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(routingKey, evento, idAlumno);
                }
            });
        } else {
            enviar(routingKey, evento, idAlumno);
        }
    }

    private void enviar(String routingKey, Object evento, Long idAlumno) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, evento);
        } catch (AmqpException e) {
            log.warn("No se pudo publicar el evento {} del alumno {} en RabbitMQ " +
                    "(¿esta levantado el broker?). El cambio igual se guardo. Detalle: {}",
                    routingKey, idAlumno, e.getMessage());
        }
    }
}
