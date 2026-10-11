package com.app.mapper;

/**
 * Convierte "algo" (normalmente un DTO) en una entidad.
 * Los DTO de alta implementan esta interfaz, asi el servicio no necesita conocer
 * el DTO concreto: solo llama a mapperTo().
 */
public interface IMapper<E> {

    E mapperTo();

}
