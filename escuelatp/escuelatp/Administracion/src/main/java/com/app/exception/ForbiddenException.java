package com.app.exception;

/** El usuario esta autenticado pero no tiene permiso para esa accion -> 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
