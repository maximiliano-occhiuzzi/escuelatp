import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { Alumno } from '../../models/alumno.model';
import { Curso } from '../../models/curso.model';
import { Rol } from '../../models/auth.model';
import { ServiciosService } from '../../service/servicios.service';
import { AuthService } from '../../service/auth.service';
import { mensajeError } from '../../shared/mensaje-error';
import { etiquetaCurso } from '../../shared/curso-util';

@Component({
  selector: 'app-list-alumno',
  templateUrl: './list-alumno.component.html',
  styleUrls: ['./list-alumno.component.css']
})
export class ListAlumnoComponent implements OnInit {

  alumnos: Alumno[] = [];
  cursosPorId = new Map<number, Curso>();
  cargando = true;
  error: string | null = null;
  mensaje: string | null = null;
  filtro = '';
  deshabilitando: number | null = null;
  habilitando: number | null = null;
  asignandoId: number | null = null;   // alumno que tiene abierto el selector de curso
  cursoElegido: number | null = null;
  guardandoCurso = false;

  readonly rolesPreceptor: Rol[] = ['PRECEPTOR'];

  constructor(
    private serviciosService: ServiciosService,
    private authService: AuthService
  ) { }

  get alumnosFiltrados(): Alumno[] {
    const f = this.filtro.trim().toLowerCase();
    if (!f) { return this.alumnos; }
    return this.alumnos.filter(a =>
      [a.nombre, a.apellido, a.dni, a.email, a.estado].some(v => (v ?? '').toString().toLowerCase().includes(f)));
  }

  get username(): string | null {
    return this.authService.getUsername();
  }

  get mensajeBienvenida(): string {
    switch (this.authService.getRol()) {
      case 'ROOT': return 'Sos ROOT: podés administrar usuarios, personal y cursos.';
      case 'DIRECTOR': return 'Como director podés consultar alumnos, cursos, personal y notas.';
      case 'PRECEPTOR': return 'Como preceptor gestionás alumnos, cursos y personal.';
      default: return 'Como docente podés ver tus cursos y cargar notas.';
    }
  }

  nombreCurso(id?: number | null): string {
    if (id == null) { return ''; }
    return this.cursosPorId.get(id)?.materia ?? `Curso ${id}`;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = null;

    forkJoin({
      alumnos: this.serviciosService.listarAlumnos(),
      cursos: this.serviciosService.listarCursos()
    }).subscribe({
      next: ({ alumnos, cursos }) => {
        this.alumnos = alumnos;
        this.cursosPorId = new Map(cursos.map(c => [c.id, c]));
        this.cargando = false;
      },
      error: (err: HttpErrorResponse) => {
        this.error = mensajeError(err, 'No se pudo obtener el listado.');
        this.cargando = false;
      }
    });
  }

  // Los alumnos no se eliminan: pasan a DESHABILITADO y se liberan del curso.
  deshabilitar(alumno: Alumno): void {
    if (!confirm(`¿Deshabilitar a ${alumno.nombre} ${alumno.apellido}? Se lo saca de su curso (el alumno no se borra).`)) {
      return;
    }
    this.deshabilitando = alumno.id;
    this.error = null;
    this.mensaje = null;

    this.serviciosService.deshabilitarAlumno(alumno.id).subscribe({
      next: (actualizado) => {
        this.deshabilitando = null;
        this.alumnos = this.alumnos.map(a => a.id === actualizado.id ? actualizado : a);
        this.mensaje = `${actualizado.nombre} ${actualizado.apellido} quedó DESHABILITADO.`;
      },
      error: (err: HttpErrorResponse) => {
        this.deshabilitando = null;
        this.error = mensajeError(err, 'No se pudo deshabilitar el alumno.');
      }
    });
  }

  // Cursos a los que se puede mandar al alumno: con cupo y distintos del actual.
  cursosDisponibles(alumno: Alumno): Curso[] {
    return [...this.cursosPorId.values()]
      .filter(c => c.id !== alumno.cursoId && c.inscriptos < c.cupoMaximo);
  }

  etiqueta(c: Curso): string {
    return `${etiquetaCurso(c)} — ${c.inscriptos}/${c.cupoMaximo}`;
  }

  abrirAsignar(alumno: Alumno): void {
    this.asignandoId = alumno.id;
    this.cursoElegido = null;
    this.error = null;
    this.mensaje = null;
  }

  cancelarAsignar(): void {
    this.asignandoId = null;
    this.cursoElegido = null;
  }

  confirmarAsignar(alumno: Alumno): void {
    if (this.cursoElegido == null) { return; }
    this.guardandoCurso = true;
    this.error = null;

    this.serviciosService.asignarCurso(alumno.id, this.cursoElegido).subscribe({
      next: (actualizado) => {
        this.guardandoCurso = false;
        this.asignandoId = null;
        this.mensaje = `${actualizado.nombre} ${actualizado.apellido} fue asignado al curso. Cursos se entera por RabbitMQ; refrescá en un momento para ver el cupo actualizado.`;
        // El cupo de los cursos cambia (async): se recarga todo.
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoCurso = false;
        // 409: sin cupo / deshabilitado; 400: curso inexistente. El mensaje viene del backend.
        this.error = mensajeError(err, 'No se pudo asignar el curso.');
      }
    });
  }

  // Vuelve a SINASIGNACION (sin curso).
  habilitar(alumno: Alumno): void {
    this.habilitando = alumno.id;
    this.error = null;
    this.mensaje = null;

    this.serviciosService.habilitarAlumno(alumno.id).subscribe({
      next: (actualizado) => {
        this.habilitando = null;
        this.alumnos = this.alumnos.map(a => a.id === actualizado.id ? actualizado : a);
        this.mensaje = `${actualizado.nombre} ${actualizado.apellido} fue habilitado (sin curso).`;
      },
      error: (err: HttpErrorResponse) => {
        this.habilitando = null;
        this.error = mensajeError(err, 'No se pudo habilitar el alumno.');
      }
    });
  }
}
