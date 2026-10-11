package com.app.service;

import com.app.dto.LoginRequest;
import com.app.dto.LoginResponse;
import com.app.dto.RefreshTokenRequest;
import com.app.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    /** Crea un usuario nuevo. Solo lo puede llamar ROOT (lo exige SecurityConfig). */
    void register(RegisterRequest request);
}
