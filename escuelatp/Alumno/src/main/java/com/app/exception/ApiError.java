package com.app.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato unico de error de la API. Todos los errores (404, 400, 403, 409, 500)
 * se devuelven con esta forma, asi el front siempre sabe como leerlos.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {}
