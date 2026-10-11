package com.app.exception;

/** Credenciales o token invalidos -> 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
