import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { Rol } from '../models/auth.model';

// Guard por rol. Los roles permitidos se declaran en la ruta:
//   { path: 'cursoForm', ..., canActivate: [authGuard, roleGuard], data: { roles: ['ROOT', 'PRECEPTOR'] } }
// Si el rol no alcanza, vuelve a la pantalla inicial. Es comodidad de UI:
// la seguridad real la hace el Gateway (403).
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const roles = (route.data['roles'] ?? []) as Rol[];

  if (authService.isLoggedIn() && authService.hasRole(...roles)) {
    return true;
  }

  router.navigate([authService.isLoggedIn() ? authService.homeRoute() : '/login']);
  return false;
};
