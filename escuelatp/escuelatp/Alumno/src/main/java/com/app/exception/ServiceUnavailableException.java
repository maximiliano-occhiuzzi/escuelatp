package com.app.exception;

/** Otro microservicio del que dependemos no responde -> 503. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
