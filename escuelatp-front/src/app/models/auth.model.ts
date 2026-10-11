export type Rol = 'ROOT' | 'DIRECTOR' | 'PRECEPTOR' | 'DOCENTE';

export const ROLES: Rol[] = ['ROOT', 'DIRECTOR', 'PRECEPTOR', 'DOCENTE'];

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  refreshToken: string;
  username: string;
  rol: Rol;
  expiresInSeconds: number;
  personalId?: number | null;
}

// Coincide con RegisterRequest del backend (solo ROOT puede registrar usuarios).
export interface RegisterRequest {
  username: string;
  password: string;
  rol: Rol;
  personalId?: number | null; // obligatorio solo si el rol es DOCENTE
}
