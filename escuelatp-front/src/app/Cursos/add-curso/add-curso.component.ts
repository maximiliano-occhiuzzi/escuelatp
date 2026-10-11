import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ServiciosService } from '../../service/servicios.service';
import { Personal } from '../../models/personal.model';
import { DIAS } from '../../models/curso.model';
import { mensajeError } from '../../shared/mensaje-error';

@Component({
  selector: 'app-add-curso',
  templateUrl: './add-curso.component.html',
  styleUrls: ['./add-curso.component.css']
})
export class AddCursoComponent implements OnInit {

  form: FormGroup;
  dias = DIAS;
  docentes: Personal[] = [];
  enviando = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private serviciosService: ServiciosService,
    private router: Router
  ) {
    this.form = this.fb.group({
      materia: ['', Validators.required],
      diaSemana: ['', Validators.required],
      horaInicio: ['', Validators.required],
      horaFin: ['', Validators.required],
      cupoMaximo: [30, [Validators.required, Validators.min(1)]],
      cicloLectivo: [new Date().getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2100)]],
      docenteId: [null]
    }, { validators: this.horarioValido });
  }

  // La hora de fin tiene que ser posterior a la de inicio.
  private horarioValido(g: any) {
    const ini = g.get('horaInicio')?.value;
    const fin = g.get('horaFin')?.value;
    return ini && fin && fin <= ini ? { horarioInvalido: true } : null;
  }

  ngOnInit(): void {
    // Solo se pueden asignar docentes del personal.
    this.serviciosService.listarPersonal().subscribe({
      next: (data) => this.docentes = data.filter(p => p.cargo === 'DOCENTE'),
      error: (err) => console.error(err)
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.error = null;

    this.serviciosService.altaCurso(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/listCurso']);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        this.error = mensajeError(err, 'No se pudo dar de alta el curso.');
      }
    });
  }
}
