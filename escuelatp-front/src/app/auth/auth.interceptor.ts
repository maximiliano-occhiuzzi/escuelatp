import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const requestConToken = this.agregarToken(req, this.authService.getToken());

    return next.handle(requestConToken).pipe(
      catchError((error: HttpErrorResponse) => {
        // No intentamos refrescar si el 401 vino del propio login o del
        // propio refresh — ahí un refresh token nuevo no soluciona nada,
        // y evita un loop infinito reintentando para siempre.
        const esRequestDeAuth = req.url.includes('/auth/login') || req.url.includes('/auth/refresh');

        if (error.status === 401 && !esRequestDeAuth && this.authService.getRefreshToken()) {
          return this.authService.refresh().pipe(
            switchMap(() => {
              // Con el access token ya renovado, reintentamos la request
              // original UNA vez, con el token nuevo.
              const reintento = this.agregarToken(req, this.authService.getToken());
              return next.handle(reintento);
            }),
            catchError(errorDeRefresh => {
              // El refresh token también está vencido/revocado: ahí sí,
              // no hay forma de recuperar la sesión sin loguearse de nuevo.
              this.authService.logout();
              this.router.navigate(['/login']);
              return throwError(() => errorDeRefresh);
            })
          );
        }

        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }

        return throwError(() => error);
      })
    );
  }

  private agregarToken(req: HttpRequest<any>, token: string | null): HttpRequest<any> {
    return token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;
  }
}
