package com.app.controller;

import com.app.dto.AlumnoAltaDTO;
import com.app.dto.AlumnoCursoDTO;
import com.app.dto.AlumnoDTO;
import com.app.service.AlumnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alumnos", description = "Alta, consulta y baja logica de alumnos")
@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    // Alta de alumno (solo PRECEPTOR, lo exige el Gateway).
    // El DTO se convierte en entidad con IMapper (dto.mapperTo()).
    @Operation(summary = "Alta de alumno",
            description = "Si viene con cursoId queda ASIGNADO y se avisa a Cursos por RabbitMQ; si no, SINASIGNACION")
    @PutMapping("/add")
    public ResponseEntity<Void> altaAlumno(@Valid @RequestBody AlumnoAltaDTO dto) {
        alumnoService.altaAlumno(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar alumnos")
    @GetMapping
    public ResponseEntity<List<AlumnoDTO>> listarAlumnos() {
        return ResponseEntity.ok(alumnoService.listarAlumnos());
    }

    @Operation(summary = "Obtener un alumno por id")
    @GetMapping("/{id}")
    public ResponseEntity<AlumnoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoService.obtenerPorId(id));
    }

    // Los alumnos no se eliminan: se deshabilitan (solo PRECEPTOR, lo exige el Gateway).
    @Operation(summary = "Deshabilitar alumno (baja logica)",
            description = "Pasa a DESHABILITADO, lo saca de su curso y libera el cupo (evento RabbitMQ)")
    @PatchMapping("/{id}/deshabilitar")
    public ResponseEntity<AlumnoDTO> deshabilitarAlumno(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoService.deshabilitarAlumno(id));
    }

    @Operation(summary = "Habilitar un alumno deshabilitado",
            description = "Vuelve a SINASIGNACION (sin curso)")
    @PatchMapping("/{id}/habilitar")
    public ResponseEntity<AlumnoDTO> habilitarAlumno(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoService.habilitarAlumno(id));
    }

    @Operation(summary = "Asignar o cambiar el curso de un alumno",
            description = "Valida que el curso exista y tenga cupo; Cursos se entera por RabbitMQ")
    @PatchMapping("/{id}/curso")
    public ResponseEntity<AlumnoDTO> asignarCurso(@PathVariable Long id, @Valid @RequestBody AlumnoCursoDTO dto) {
        return ResponseEntity.ok(alumnoService.asignarCurso(id, dto.cursoId()));
    }
}
