import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Curso } from '../../models/curso.model';
import { Rol } from '../../models/auth.model';
import { ServiciosService } from '../../service/servicios.service';
import { AuthService } from '../../service/auth.service';
import { mensajeError } from '../../shared/mensaje-error';
import { nombreDia, hora } from '../../shared/curso-util';

@Component({
  selector: 'app-list-curso',
  templateUrl: './list-curso.component.html',
  styleUrls: ['./list-curso.component.css']
})
export class ListCursoComponent implements OnInit {

  cursos: Curso[] = [];
  cargando = true;
  error: string | null = null;
  filtro = '';

  readonly rolesAlta: Rol[] = ['ROOT', 'PRECEPTOR'];
  readonly rolesNotas: Rol[] = ['DOCENTE', 'DIRECTOR'];

  nombreDia = nombreDia;
  hora = hora;

  constructor(
    private serviciosService: ServiciosService,
    private authService: AuthService
  ) { }

  get esDocente(): boolean {
    return this.authService.getRol() === 'DOCENTE';
  }

  get cursosFiltrados(): Curso[] {
    const f = this.filtro.trim().toLowerCase();
    if (!f) { return this.cursos; }
    return this.cursos.filter(c =>
      [c.materia, nombreDia(c.diaSemana), c.cicloLectivo].some(v => (v ?? '').toString().toLowerCase().includes(f)));
  }

  // Porcentaje de cupo ocupado (0-100) para la barra de progreso.
  cupoPorcentaje(curso: Curso): number {
    if (!curso.cupoMaximo) { return 0; }
    return Math.min(100, Math.round((curso.inscriptos / curso.cupoMaximo) * 100));
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = null;

    this.serviciosService.listarCursos().subscribe({
      next: (data) => {
        this.cursos = data;
        this.cargando = false;
      },
      error: (err: HttpErrorResponse) => {
        this.error = mensajeError(err, 'No se pudo obtener el listado.');
        this.cargando = false;
      }
    });
  }
}
