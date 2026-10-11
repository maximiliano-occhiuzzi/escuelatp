import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Alumno, AlumnoAlta } from '../models/alumno.model';
import { Curso, CursoAlta } from '../models/curso.model';
import { Personal, PersonalAlta } from '../models/personal.model';
import { Nota, NotaAlta, NotaModificar } from '../models/nota.model';
import { API_URL } from '../config';

// Todas las llamadas van al Gateway, que enruta:
// /alumnos/** -> modulo-alumnos, /cursos/** -> modulo-cursos, /personal/** -> modulo-administracion
@Injectable({
  providedIn: 'root'
})
export class ServiciosService {

  private readonly urlAlumnos = `${API_URL}/alumnos`;
  private readonly urlCursos = `${API_URL}/cursos`;
  private readonly urlPersonal = `${API_URL}/personal`;

  constructor(private http: HttpClient) { }

  // ---- Alumnos ----
  // El alta es PUT /alumnos/add (el backend responde 201 sin cuerpo).
  altaAlumno(alumno: AlumnoAlta): Observable<void> {
    return this.http.put<void>(`${this.urlAlumnos}/add`, alumno);
  }

  listarAlumnos(): Observable<Alumno[]> {
    return this.http.get<Alumno[]>(this.urlAlumnos);
  }

  // Los alumnos nunca se borran: se deshabilitan.
  deshabilitarAlumno(id: number): Observable<Alumno> {
    return this.http.patch<Alumno>(`${this.urlAlumnos}/${id}/deshabilitar`, null);
  }

  // Reactiva un alumno deshabilitado (vuelve sin curso).
  habilitarAlumno(id: number): Observable<Alumno> {
    return this.http.patch<Alumno>(`${this.urlAlumnos}/${id}/habilitar`, null);
  }

  // Asigna o cambia el curso de un alumno (el backend valida curso y cupo).
  asignarCurso(id: number, cursoId: number): Observable<Alumno> {
    return this.http.patch<Alumno>(`${this.urlAlumnos}/${id}/curso`, { cursoId });
  }

  // ---- Cursos ----
  altaCurso(curso: CursoAlta): Observable<Curso> {
    return this.http.post<Curso>(this.urlCursos, curso);
  }

  listarCursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(this.urlCursos);
  }

  obtenerCurso(id: number): Observable<Curso> {
    return this.http.get<Curso>(`${this.urlCursos}/${id}`);
  }

  // ---- Notas (dentro de un curso) ----
  listarNotas(cursoId: number): Observable<Nota[]> {
    return this.http.get<Nota[]>(`${this.urlCursos}/${cursoId}/notas`);
  }

  altaNota(cursoId: number, nota: NotaAlta): Observable<Nota> {
    return this.http.post<Nota>(`${this.urlCursos}/${cursoId}/notas`, nota);
  }

  modificarNota(cursoId: number, notaId: number, nota: NotaModificar): Observable<Nota> {
    return this.http.put<Nota>(`${this.urlCursos}/${cursoId}/notas/${notaId}`, nota);
  }

  eliminarNota(cursoId: number, notaId: number): Observable<void> {
    return this.http.delete<void>(`${this.urlCursos}/${cursoId}/notas/${notaId}`);
  }

  // ---- Personal ----
  altaPersonal(personal: PersonalAlta): Observable<Personal> {
    return this.http.post<Personal>(this.urlPersonal, personal);
  }

  modificarPersonal(id: number, personal: PersonalAlta): Observable<Personal> {
    return this.http.put<Personal>(`${this.urlPersonal}/${id}`, personal);
  }

  obtenerPersonal(id: number): Observable<Personal> {
    return this.http.get<Personal>(`${this.urlPersonal}/${id}`);
  }

  listarPersonal(): Observable<Personal[]> {
    return this.http.get<Personal[]>(this.urlPersonal);
  }
}
