import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AuthService } from '../../service/auth.service';
import { ServiciosService } from '../../service/servicios.service';
import { Personal } from '../../models/personal.model';
import { ROLES } from '../../models/auth.model';
import { mensajeError } from '../../shared/mensaje-error';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  form: FormGroup;
  roles = ROLES;
  docentes: Personal[] = [];
  enviando = false;
  error: string | null = null;
  mensajeExito: string | null = null;

  // Observable: el selector de docente solo se muestra si el rol elegido es DOCENTE.
  esDocente$: Observable<boolean>;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private serviciosService: ServiciosService
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(4)]],
      rol: ['PRECEPTOR', Validators.required],
      personalId: [null]
    });

    this.esDocente$ = this.form.get('rol')!.valueChanges.pipe(map(rol => rol === 'DOCENTE'));

    // El personalId es obligatorio solo para el rol DOCENTE.
    this.form.get('rol')!.valueChanges.subscribe(rol => {
      const ctrl = this.form.get('personalId')!;
      if (rol === 'DOCENTE') {
        ctrl.setValidators(Validators.required);
      } else {
        ctrl.clearValidators();
        ctrl.setValue(null);
      }
      ctrl.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    // Los docentes disponibles para asociar al usuario.
    this.serviciosService.listarPersonal().subscribe({
      next: (data) => this.docentes = data.filter(p => p.cargo === 'DOCENTE'),
      error: (err) => console.error(err)
    });
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.error = null;
    this.mensajeExito = null;

    this.authService.register(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        this.mensajeExito = `Usuario "${this.form.value.username}" creado con rol ${this.form.value.rol}.`;
        this.form.reset({ rol: 'PRECEPTOR', personalId: null });
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        this.error = err.status === 409
          ? 'Ese nombre de usuario ya existe.'
          : mensajeError(err, 'No se pudo crear el usuario.');
      }
    });
  }
}
