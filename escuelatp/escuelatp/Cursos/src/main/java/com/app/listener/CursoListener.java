package com.app.listener;

import com.app.config.RabbitMQConsumerConfig;
import com.app.listener.objects.AlumnoBajaEvent;
import com.app.listener.objects.AlumnoEvent;
import com.app.model.Alumno;
import com.app.model.Curso;
import com.app.repository.AlumnoRepository;
import com.app.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer de los eventos que publica el microservicio Alumno:
 *  - alta: un alumno se inscribio en un curso   -> se lo suma a la lista del curso.
 *  - baja: un alumno se deshabilito              -> se lo saca del curso (libera cupo).
 *
 * Si algo falla (curso inexistente, evento incompleto) se lanza una excepcion:
 * con retry deshabilitado y default-requeue-rejected=false (ver
 * modulo-cursos-*.yml) el mensaje se descarta y NO queda reintentandose
 * en loop.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CursoListener {

    private final CursoRepository cursoRepository;
    private final AlumnoRepository alumnoRepository;

    @RabbitListener(queues = RabbitMQConsumerConfig.QUEUE)
    @Transactional
    public void recibirAltaAlumno(AlumnoEvent evento) {
        if (evento.getIdAlumno() == null || evento.getIdCurso() == null) {
            throw new IllegalArgumentException("Evento incompleto: " + evento);
        }

        Curso curso = cursoRepository.findById(evento.getIdCurso())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el curso " + evento.getIdCurso() + " (evento: " + evento + ")"));

        // Idempotencia: si el mensaje se entrega dos veces, no se duplica el alumno
        boolean yaEstaba = curso.getAlumnos().stream()
                .anyMatch(a -> evento.getIdAlumno().equals(a.getId()));
        if (yaEstaba) {
            log.info("El alumno {} ya estaba en el curso {}, se ignora el evento",
                    evento.getIdAlumno(), evento.getIdCurso());
            return;
        }

        // Cupo: no se puede superar el cupo maximo del curso.
        if (curso.getAlumnos().size() >= curso.getCupoMaximo()) {
            log.error("Cupo completo en el curso {} ({} alumnos): no se pudo inscribir al alumno {}",
                    curso.getId(), curso.getCupoMaximo(), evento.getIdAlumno());
            return;
        }

        // Si el alumno ya estaba inscripto en OTRO curso (cambio de curso), primero se lo
        // saca de ahi: su id es la clave de la tabla y no puede estar en dos cursos.
        // saveAndFlush fuerza el DELETE antes del INSERT de abajo.
        alumnoRepository.findById(evento.getIdAlumno()).ifPresent(existente -> {
            Curso anterior = existente.getCurso();
            if (anterior != null && !anterior.getId().equals(curso.getId())) {
                anterior.removeAlumno(existente);
                cursoRepository.saveAndFlush(anterior);
            }
        });

        curso.addAlumno(new Alumno(evento.getIdAlumno()));
        cursoRepository.save(curso);

        log.info("Evento recibido: alumno {} agregado al curso {}",
                evento.getIdAlumno(), evento.getIdCurso());
    }

    @RabbitListener(queues = RabbitMQConsumerConfig.BAJA_QUEUE)
    @Transactional
    public void recibirBajaAlumno(AlumnoBajaEvent evento) {
        if (evento.getIdAlumno() == null || evento.getIdCurso() == null) {
            throw new IllegalArgumentException("Evento de baja incompleto: " + evento);
        }

        Curso curso = cursoRepository.findById(evento.getIdCurso())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el curso " + evento.getIdCurso() + " (evento: " + evento + ")"));

        Alumno alumno = curso.getAlumnos().stream()
                .filter(a -> evento.getIdAlumno().equals(a.getId()))
                .findFirst()
                .orElse(null);

        if (alumno == null) {
            log.info("El alumno {} no estaba en el curso {}, se ignora el evento de baja",
                    evento.getIdAlumno(), evento.getIdCurso());
            return;
        }

        curso.removeAlumno(alumno); // orphanRemoval borra la fila y se libera el cupo
        cursoRepository.save(curso);

        log.info("Evento recibido: alumno {} dado de baja del curso {}",
                evento.getIdAlumno(), evento.getIdCurso());
    }
}
