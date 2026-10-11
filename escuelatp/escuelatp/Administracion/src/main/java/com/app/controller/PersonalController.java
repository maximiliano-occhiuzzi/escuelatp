package com.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.PersonalAltaDTO;
import com.app.dto.PersonalDTO;
import com.app.service.PersonalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Personal", description = "Gestion de docentes, administrativos y demas personal del colegio")
@RestController
@RequestMapping("/personal")
public class PersonalController {

	private final PersonalService personalService;

	public PersonalController(PersonalService personalService) {
		this.personalService = personalService;
	}

	@Operation(summary = "Alta de personal (ROOT, PRECEPTOR)")
	@PostMapping
	public ResponseEntity<PersonalDTO> altaPersonal(@Valid @RequestBody PersonalAltaDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(personalService.altaPersonal(dto));
	}

	@Operation(summary = "Modificar personal (ROOT, PRECEPTOR)")
	@PutMapping("/{id}")
	public ResponseEntity<PersonalDTO> modificarPersonal(@PathVariable Long id,
			@Valid @RequestBody PersonalAltaDTO dto) {
		return ResponseEntity.ok(personalService.modificarPersonal(id, dto));
	}

	@Operation(summary = "Listar personal (ROOT, PRECEPTOR, DIRECTOR)")
	@GetMapping
	public ResponseEntity<List<PersonalDTO>> listarPersonal() {
		return ResponseEntity.ok(personalService.listarPersonal());
	}

	@Operation(summary = "Obtener personal por id")
	@GetMapping("/{id}")
	public ResponseEntity<PersonalDTO> obtenerPorId(@PathVariable Long id) {
		return ResponseEntity.ok(personalService.obtenerPorId(id));
	}
}
