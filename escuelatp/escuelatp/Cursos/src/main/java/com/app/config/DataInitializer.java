package com.app.config;

import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.app.enums.DiaSemana;
import com.app.model.Curso;
import com.app.repository.CursoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Curso de ejemplo, solo si la tabla esta vacia. Esta asignado al docente con
 * id 1 (el Personal que crea Administracion, asociado al usuario "docente").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    @Override
    public void run(String... args) {
        if (cursoRepository.count() > 0) {
            return;
        }

        Curso curso = new Curso();
        curso.setMateria("Matematica");
        curso.setDiaSemana(DiaSemana.LUNES);
        curso.setHoraInicio(LocalTime.of(8, 0));
        curso.setHoraFin(LocalTime.of(10, 0));
        curso.setCupoMaximo(30);
        curso.setCicloLectivo(2026);
        curso.setDocenteId(1L);
        cursoRepository.save(curso);

        log.info("Curso de ejemplo creado: Matematica, lunes 08:00-10:00, docente 1");
    }
}
