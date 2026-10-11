package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType; // "Bearer"
    private String refreshToken;
    private String username;
    private String rol;
    private long expiresInSeconds;
    private Long personalId; // solo DOCENTE, si no null
}
