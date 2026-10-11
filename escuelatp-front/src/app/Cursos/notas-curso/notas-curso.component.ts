import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ServiciosService } from '../../service/servicios.service';
import { Curso } from '../../models/curso.model';
import { Alumno } from '../../models/alumno.model';
import { Nota } from '../../models/nota.model';
import { Rol } from '../../models/auth.model';
import { mensajeError } from '../../shared/mensaje-error';
import { etiquetaCurso } from '../../shared/curso-util';

@Component({
  selector: 'app-notas-curso',
  templateUrl: './notas-curso.component.html',
  styleUrls: ['./notas-curso.component.css']
})
export class NotasCursoComponent implements OnInit {

  cursoId!: number;
  curso: Curso | null = null;
  notas: Nota[] = [];
  alumnos = new Map<number, Alumno>();   // para mostrar nombres en vez de ids
  inscriptos: Alumno[] = [];             // alumnos del curso (para el selector)

  form: FormGroup;
  editandoId: number | null = null;      // null = cargando una nota nueva
  cargando = true;
  enviando = false;
  error: string | null = null;
  mensaje: string | null = null;

  // Solo el DOCENTE carga/modifica/borra; el DIRECTOR solo consulta.
  readonly rolesDocente: Rol[] = ['DOCENTE'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private serviciosService: ServiciosService
  ) {
    this.form = this.fb.group({
      alumnoId: [null, Validators.required],
      valor: [null, [Validators.required, Validators.min(1), Validators.max(10)]],
      descripcion: [''],
      fecha: ['']
    });
  }

  get titulo(): string {
    return this.curso ? etiquetaCurso(this.curso) : `Curso ${this.cursoId}`;
  }

  ngOnInit(): void {
    this.cursoId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }

  nombreAlumno(id: number): string {
    const a = this.alumnos.get(id);
    return a ? `${a.apellido}, ${a.nombre}` : `Alumno #${id}`;
  }

  promedio(alumnoId: number): number | null {
    const mias = this.notas.filter(n => n.alumnoId === alumnoId);
    if (!mias.length) { return null; }
    return mias.reduce((s, n) => s + n.valor, 0) / mias.length;
  }

  cargar(): void {
    this.cargando = true;
    this.error = null;

    forkJoin({
      curso: this.serviciosService.obtenerCurso(this.cursoId),
      notas: this.serviciosService.listarNotas(this.cursoId),
      alumnos: this.serviciosService.listarAlumnos()
    }).subscribe({
      next: ({ curso, notas, alumnos }) => {
        this.curso = curso;
        this.notas = notas;
        this.alumnos = new Map(alumnos.map(a => [a.id, a]));
        this.inscriptos = alumnos.filter(a => curso.alumnosIds.includes(a.id));
        this.cargando = false;
      },
      error: (err: HttpErrorResponse) => {
        this.error = mensajeError(err, 'No se pudieron cargar las notas del curso.');
        this.cargando = false;
      }
    });
  }

  nueva(): void {
    this.editandoId = null;
    this.form.reset({ alumnoId: null, valor: null, descripcion: '', fecha: '' });
    this.form.get('alumnoId')!.enable();
  }

  editar(nota: Nota): void {
    this.editandoId = nota.id;
    this.mensaje = null;
    this.form.reset({
      alumnoId: nota.alumnoId,
      valor: nota.valor,
      descripcion: nota.descripcion ?? '',
      fecha: nota.fecha
    });
    // El alumno de una nota no se puede cambiar.
    this.form.get('alumnoId')!.disable();
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.error = null;
    this.mensaje = null;

    const v = this.form.getRawValue();
    const datos = { valor: Number(v.valor), descripcion: v.descripcion || null, fecha: v.fecha || null };

    const peticion = this.editandoId == null
      ? this.serviciosService.altaNota(this.cursoId, { alumnoId: v.alumnoId, ...datos })
      : this.serviciosService.modificarNota(this.cursoId, this.editandoId, datos);

    peticion.subscribe({
      next: () => {
        this.enviando = false;
        this.mensaje = this.editandoId == null ? 'Nota cargada.' : 'Nota modificada.';
        this.nueva();
        this.recargarNotas();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        this.error = mensajeError(err, 'No se pudo guardar la nota.');
      }
    });
  }

  eliminar(nota: Nota): void {
    if (!confirm(`¿Borrar la nota ${nota.valor} de ${this.nombreAlumno(nota.alumnoId)}?`)) {
      return;
    }
    this.error = null;
    this.mensaje = null;
    this.serviciosService.eliminarNota(this.cursoId, nota.id).subscribe({
      next: () => {
        this.mensaje = 'Nota eliminada.';
        this.recargarNotas();
      },
      error: (err: HttpErrorResponse) => this.error = mensajeError(err, 'No se pudo borrar la nota.')
    });
  }

  private recargarNotas(): void {
    this.serviciosService.listarNotas(this.cursoId).subscribe({
      next: (n) => this.notas = n,
      error: (err: HttpErrorResponse) => this.error = mensajeError(err, 'No se pudieron recargar las notas.')
    });
  }
}
