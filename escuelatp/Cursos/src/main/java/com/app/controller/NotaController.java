package com.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.dto.NotaAltaDTO;
import com.app.dto.NotaDTO;
import com.app.dto.NotaModificarDTO;
import com.app.security.UsuarioActual;
import com.app.service.NotaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Notas de un curso: /cursos/{cursoId}/notas
 *
 *   GET            -> DOCENTE (sus cursos) y DIRECTOR (todos)
 *   POST/PUT/DELETE -> solo DOCENTE, y solo en sus cursos
 * El Gateway filtra por rol; NotaService controla que el docente sea el del curso.
 */
@Tag(name = "Notas", description = "Notas de los alumnos en un curso")
@RestController
@RequestMapping("/cursos/{cursoId}/notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @Operation(summary = "Listar las notas de un curso (DOCENTE de ese curso, DIRECTOR)")
    @GetMapping
    public ResponseEntity<List<NotaDTO>> listarNotas(@PathVariable Long cursoId, HttpServletRequest request) {
        return ResponseEntity.ok(notaService.listarNotas(cursoId, UsuarioActual.desde(request)));
    }

    @Operation(summary = "Agregar una nota (solo el DOCENTE del curso)")
    @PostMapping
    public ResponseEntity<NotaDTO> agregarNota(@PathVariable Long cursoId,
            @Valid @RequestBody NotaAltaDTO dto, HttpServletRequest request) {
        NotaDTO creada = notaService.agregarNota(cursoId, dto, UsuarioActual.desde(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @Operation(summary = "Modificar una nota (solo el DOCENTE del curso)")
    @PutMapping("/{notaId}")
    public ResponseEntity<NotaDTO> modificarNota(@PathVariable Long cursoId, @PathVariable Long notaId,
            @Valid @RequestBody NotaModificarDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(notaService.modificarNota(cursoId, notaId, dto, UsuarioActual.desde(request)));
    }

    @Operation(summary = "Eliminar una nota (solo el DOCENTE del curso)")
    @DeleteMapping("/{notaId}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long cursoId, @PathVariable Long notaId,
            HttpServletRequest request) {
        notaService.eliminarNota(cursoId, notaId, UsuarioActual.desde(request));
        return ResponseEntity.noContent().build();
    }
}
