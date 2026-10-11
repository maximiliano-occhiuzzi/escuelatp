package com.app.exception;

/** La operacion choca con el estado actual (duplicado, cupo lleno, etc.) -> 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
