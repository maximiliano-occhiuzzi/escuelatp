import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { mensajeError } from '../../shared/mensaje-error';
import { ServiciosService } from '../../service/servicios.service';

@Component({
  selector: 'app-add-personal',
  templateUrl: './add-personal.component.html',
  styleUrls: ['./add-personal.component.css']
})
export class AddPersonalComponent implements OnInit {

  form: FormGroup;
  enviando = false;
  error: string | null = null;
  editandoId: number | null = null; // si viene :id en la ruta, se modifica en vez de crear

  constructor(
    private fb: FormBuilder,
    private serviciosService: ServiciosService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      cargo: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editandoId = Number(id);
      this.serviciosService.obtenerPersonal(this.editandoId).subscribe({
        next: (p) => this.form.patchValue(p),
        error: (err: HttpErrorResponse) => this.error = mensajeError(err, 'No se pudo cargar el personal.')
      });
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.error = null;

    const peticion = this.editandoId == null
      ? this.serviciosService.altaPersonal(this.form.value)
      : this.serviciosService.modificarPersonal(this.editandoId, this.form.value);

    peticion.subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/listPersonal']);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        this.error = err.status === 409
          ? 'Ya existe una persona con ese email.'
          : mensajeError(err, 'No se pudo guardar el personal.');
      }
    });
  }
}
