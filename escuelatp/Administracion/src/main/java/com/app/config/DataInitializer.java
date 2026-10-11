package com.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.app.Enum.Cargo;
import com.app.model.Personal;
import com.app.repository.PersonalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Personal de prueba, solo si la tabla esta vacia.
 *
 * El primero (id 1) es el DOCENTE al que apunta el usuario "docente" que crea
 * Auth al arrancar (personalId = 1) y el curso de ejemplo que crea Cursos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PersonalRepository personalRepository;

    @Override
    public void run(String... args) {
        if (personalRepository.count() > 0) {
            return;
        }
        personalRepository.save(new Personal(null, "Ana", "Lopez", "ana.lopez@escuela.com", Cargo.DOCENTE));
        personalRepository.save(new Personal(null, "Laura", "Gomez", "laura.gomez@escuela.com", Cargo.DIRECTOR));
        log.info("Personal de prueba creado: Ana Lopez (DOCENTE, id 1) y Laura Gomez (DIRECTOR, id 2)");
    }
}
