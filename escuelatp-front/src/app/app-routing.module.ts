import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddAlumnoComponent } from './Alumnos/add-alumno/add-alumno.component';
import { ListAlumnoComponent } from './Alumnos/list-alumno/list-alumno.component';
import { AddCursoComponent } from './Cursos/add-curso/add-curso.component';
import { ListCursoComponent } from './Cursos/list-curso/list-curso.component';
import { NotasCursoComponent } from './Cursos/notas-curso/notas-curso.component';
import { AddPersonalComponent } from './Personal/add-personal/add-personal.component';
import { ListPersonalComponent } from './Personal/list-personal/list-personal.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { authGuard } from './auth/auth.guard';
import { roleGuard } from './auth/role.guard';

// Cada ruta declara en data.roles qué roles pueden entrar (lo lee roleGuard).
// Debe coincidir con las reglas del Gateway.
const routes: Routes = [
  { path: '', redirectTo: 'listAlumno', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },

  { path: 'register', component: RegisterComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['ROOT'] } },

  { path: 'listAlumno', component: ListAlumnoComponent, canActivate: [authGuard] },
  { path: 'alumnoForm', component: AddAlumnoComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['PRECEPTOR'] } },

  { path: 'listCurso', component: ListCursoComponent, canActivate: [authGuard] },
  { path: 'cursoForm', component: AddCursoComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['ROOT', 'PRECEPTOR'] } },
  { path: 'cursos/:id/notas', component: NotasCursoComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['DOCENTE', 'DIRECTOR'] } },

  { path: 'listPersonal', component: ListPersonalComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['ROOT', 'PRECEPTOR', 'DIRECTOR'] } },
  { path: 'personalForm', component: AddPersonalComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['ROOT', 'PRECEPTOR'] } },
  { path: 'personalForm/:id', component: AddPersonalComponent,
    canActivate: [authGuard, roleGuard], data: { roles: ['ROOT', 'PRECEPTOR'] } },

  { path: '**', redirectTo: 'listAlumno' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
