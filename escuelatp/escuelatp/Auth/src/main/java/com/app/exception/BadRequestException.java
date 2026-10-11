package com.app.exception;

/** La peticion es invalida segun una regla de negocio -> 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
