package com.app.service;

import java.util.List;

import com.app.dto.PersonalAltaDTO;
import com.app.dto.PersonalDTO;
import com.app.mapper.IMapper;
import com.app.model.Personal;

public interface PersonalService {

    PersonalDTO altaPersonal(IMapper<Personal> mapper);

    PersonalDTO modificarPersonal(Long id, PersonalAltaDTO datos);

    PersonalDTO obtenerPorId(Long id);

    List<PersonalDTO> listarPersonal();

}
