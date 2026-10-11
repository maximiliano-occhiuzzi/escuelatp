package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    // Los cursos de un docente (para que solo vea los suyos)
    List<Curso> findByDocenteId(Long docenteId);
}
