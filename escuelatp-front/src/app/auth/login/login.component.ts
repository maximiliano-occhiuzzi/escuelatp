import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  form: FormGroup;
  enviando = false;
  error: string | null = null;
  iniciandoSesion = false;
  verPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ingresar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.error = null;

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.enviando = false;
        // El login en el servidor ya terminó acá (ya tenemos el token
        // guardado) — este delay es solo visual, para que la pantalla
        // de "Iniciando sesión..." se note antes de entrar de golpe.
        this.iniciandoSesion = true;
        setTimeout(() => {
          this.router.navigate([this.authService.homeRoute()]);
        }, 1200);
      },
      error: (err) => {
        this.enviando = false;
        this.error = err.status === 401
          ? 'Usuario o contraseña incorrectos.'
          : 'No se pudo conectar con el servidor. Revisá que el backend esté levantado.';
      }
    });
  }
}
