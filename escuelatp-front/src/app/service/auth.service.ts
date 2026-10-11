import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest, Rol } from '../models/auth.model';
import { API_URL } from '../config';

const TOKEN_KEY = 'escuelatp_token';
const REFRESH_KEY = 'escuelatp_refresh_token';
const USERNAME_KEY = 'escuelatp_username';
const ROL_KEY = 'escuelatp_rol';
const PERSONAL_KEY = 'escuelatp_personal_id';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = `${API_URL}/auth`;

  // Permite que la barra de navegación sepa en tiempo real si hay sesión.
  private loggedIn$ = new BehaviorSubject<boolean>(this.hayToken());

  constructor(private http: HttpClient) { }

  login(credenciales: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credenciales).pipe(
      tap(respuesta => this.guardarSesion(respuesta))
    );
  }

  // Cuando el access token vence, el interceptor llama acá para conseguir uno nuevo.
  refresh(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(respuesta => this.guardarSesion(respuesta))
    );
  }

  // Revoca el refresh token en el servidor (best-effort) y limpia la sesión local.
  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({ next: () => {}, error: () => {} });
    }
    [TOKEN_KEY, REFRESH_KEY, USERNAME_KEY, ROL_KEY, PERSONAL_KEY].forEach(k => localStorage.removeItem(k));
    this.loggedIn$.next(false);
  }

  register(nuevoUsuario: RegisterRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/register`, nuevoUsuario, { responseType: 'text' });
  }

  getToken(): string | null { return localStorage.getItem(TOKEN_KEY); }
  getRefreshToken(): string | null { return localStorage.getItem(REFRESH_KEY); }
  getUsername(): string | null { return localStorage.getItem(USERNAME_KEY); }
  getRol(): Rol | null { return localStorage.getItem(ROL_KEY) as Rol | null; }

  getPersonalId(): number | null {
    const id = localStorage.getItem(PERSONAL_KEY);
    return id ? Number(id) : null;
  }

  // true si el usuario tiene alguno de los roles indicados.
  // Es comodidad de UI: la seguridad real la hace el Gateway con 403.
  hasRole(...roles: Rol[]): boolean {
    const rol = this.getRol();
    return !!rol && roles.includes(rol);
  }

  isLoggedIn(): boolean { return this.hayToken(); }

  isLoggedIn$(): Observable<boolean> { return this.loggedIn$.asObservable(); }

  // Pantalla inicial según el rol: el docente arranca en sus cursos.
  homeRoute(): string {
    return this.getRol() === 'DOCENTE' ? '/listCurso' : '/listAlumno';
  }

  private guardarSesion(respuesta: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, respuesta.token);
    localStorage.setItem(REFRESH_KEY, respuesta.refreshToken);
    localStorage.setItem(USERNAME_KEY, respuesta.username);
    localStorage.setItem(ROL_KEY, respuesta.rol);
    if (respuesta.personalId != null) {
      localStorage.setItem(PERSONAL_KEY, String(respuesta.personalId));
    } else {
      localStorage.removeItem(PERSONAL_KEY);
    }
    this.loggedIn$.next(true);
  }

  private hayToken(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  }
}
