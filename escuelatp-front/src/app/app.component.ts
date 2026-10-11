import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './service/auth.service';
import { Rol } from './models/auth.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'escuelatp';

  loggedIn$: Observable<boolean>;
  cerrandoSesion = false;

  // Roles que ven cada opción del menú (igual que las reglas del Gateway).
  readonly rolesPreceptor: Rol[] = ['PRECEPTOR'];
  readonly rolesCursoAlta: Rol[] = ['ROOT', 'PRECEPTOR'];
  readonly rolesPersonalVer: Rol[] = ['ROOT', 'PRECEPTOR', 'DIRECTOR'];
  readonly rolesPersonalAlta: Rol[] = ['ROOT', 'PRECEPTOR'];
  readonly rolesRoot: Rol[] = ['ROOT'];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.loggedIn$ = this.authService.isLoggedIn$();
  }

  get username(): string | null {
    return this.authService.getUsername();
  }

  get rol(): string | null {
    return this.authService.getRol();
  }

  salir(): void {
    this.cerrandoSesion = true;
    // authService.logout() ya revoca el refresh token en el servidor; el
    // delay es solo visual para que se note la pantalla "Cerrando sesión...".
    setTimeout(() => {
      this.authService.logout();
      this.router.navigate(['/login']).then(() => {
        this.cerrandoSesion = false;
      });
    }, 1500);
  }
}
