import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ServiciosService } from '../../service/servicios.service';
import { Curso } from '../../models/curso.model';
import { HttpErrorResponse } from '@angular/common/http';
import { mensajeError } from '../../shared/mensaje-error';
import { etiquetaCurso } from '../../shared/curso-util';

@Component({
  selector: 'app-add-alumno',
  templateUrl: './add-alumno.component.html',
  styleUrls: ['./add-alumno.component.css']
})
export class AddAlumnoComponent implements OnInit {

  form: FormGroup;
  cursos: Curso[] = [];
  enviando = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private serviciosService: ServiciosService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      dni: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      cursoId: [null]
    });
  }

  // Carga los cursos para el selector. Al dar de alta un alumno con curso, el
  // backend avisa por RabbitMQ al microservicio de Cursos para inscribirlo.
  ngOnInit(): void {
    this.serviciosService.listarCursos().subscribe({
      next: (data) => this.cursos = data,
      error: (err) => console.error(err)
    });
  }

  etiqueta(c: Curso): string {
    return etiquetaCurso(c);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.error = null;

    this.serviciosService.altaAlumno(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/listAlumno']);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        // 409: DNI repetido o curso sin cupo; 400: curso inexistente. El mensaje viene del backend.
        this.error = mensajeError(err, 'No se pudo dar de alta el alumno.');
      }
    });
  }
}
