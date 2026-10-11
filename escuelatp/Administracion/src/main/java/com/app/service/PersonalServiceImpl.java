package com.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.PersonalAltaDTO;
import com.app.dto.PersonalDTO;
import com.app.exception.ConflictException;
import com.app.exception.ResourceNotFoundException;
import com.app.mapper.IMapper;
import com.app.model.Personal;
import com.app.repository.PersonalRepository;

@Service
public class PersonalServiceImpl implements PersonalService {

    private final PersonalRepository personalRepository;

    public PersonalServiceImpl(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    @Override
    @Transactional
    public PersonalDTO altaPersonal(IMapper<Personal> mapper) {
        Personal personal = mapper.mapperTo();
        personal.setId(null); // el alta nunca acepta un id externo: lo genera la base

        if (personalRepository.existsByEmail(personal.getEmail())) {
            throw new ConflictException("Ya existe personal con el email " + personal.getEmail());
        }
        return PersonalDTO.from(personalRepository.save(personal));
    }

    @Override
    @Transactional
    public PersonalDTO modificarPersonal(Long id, PersonalAltaDTO datos) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el personal con id " + id));

        if (!personal.getEmail().equalsIgnoreCase(datos.email())
                && personalRepository.existsByEmail(datos.email())) {
            throw new ConflictException("Ya existe personal con el email " + datos.email());
        }

        personal.setNombre(datos.nombre());
        personal.setApellido(datos.apellido());
        personal.setEmail(datos.email());
        personal.setCargo(datos.cargo());
        return PersonalDTO.from(personalRepository.save(personal));
    }

    @Override
    public List<PersonalDTO> listarPersonal() {
        return personalRepository.findAll().stream().map(PersonalDTO::from).toList();
    }

    @Override
    public PersonalDTO obtenerPorId(Long id) {
        return personalRepository.findById(id)
                .map(PersonalDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el personal con id " + id));
    }

}
