package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Nota;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByCursoId(Long cursoId);

    Optional<Nota> findByIdAndCursoId(Long id, Long cursoId);
}
