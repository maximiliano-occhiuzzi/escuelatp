package com.app.mapper;

/**
 * Convierte "algo" (normalmente un DTO) en una entidad.
 * El DTO de alta implementa esta interfaz, asi el servicio no necesita conocer
 * el DTO concreto: solo llama a mapperTo().
 */
public interface IMapper<E> {

    E mapperTo();

}
