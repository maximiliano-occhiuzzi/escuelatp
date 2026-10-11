package com.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.dto.CursoAltaDTO;
import com.app.dto.CursoDTO;
import com.app.security.UsuarioActual;
import com.app.service.CursoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Cursos", description = "Alta y consulta de cursos (materia, horario, cupo y docente)")
@RestController
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // Solo ROOT y PRECEPTOR (lo exige el Gateway)
    @Operation(summary = "Crear un curso (ROOT, PRECEPTOR)")
    @PostMapping
    public ResponseEntity<CursoDTO> altaCurso(@Valid @RequestBody CursoAltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.altaCurso(dto));
    }

    @Operation(summary = "Listar cursos",
            description = "El DOCENTE ve solo los suyos; el resto de los roles ve todos")
    @GetMapping
    public ResponseEntity<List<CursoDTO>> listarCursos(HttpServletRequest request) {
        return ResponseEntity.ok(cursoService.listarCursos(UsuarioActual.desde(request)));
    }

    @Operation(summary = "Obtener un curso por id",
            description = "El DOCENTE solo puede ver un curso si es el asignado (si no, 403)")
    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> obtenerPorId(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(cursoService.obtenerPorId(id, UsuarioActual.desde(request)));
    }
}
