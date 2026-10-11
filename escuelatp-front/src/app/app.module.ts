import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';



import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AddAlumnoComponent } from './Alumnos/add-alumno/add-alumno.component';
import { ListAlumnoComponent } from './Alumnos/list-alumno/list-alumno.component';
import { AddCursoComponent } from './Cursos/add-curso/add-curso.component';
import { ListCursoComponent } from './Cursos/list-curso/list-curso.component';
import { AddPersonalComponent } from './Personal/add-personal/add-personal.component';
import { ListPersonalComponent } from './Personal/list-personal/list-personal.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { NotasCursoComponent } from './Cursos/notas-curso/notas-curso.component';
import { HasRoleDirective } from './auth/has-role.directive';
import { LoadingOverlayComponent } from './shared/loading-overlay/loading-overlay.component';
import { AuthInterceptor } from './auth/auth.interceptor';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@NgModule({
  declarations: [
    AppComponent,
    AddAlumnoComponent,
    ListAlumnoComponent,
    AddCursoComponent,
    ListCursoComponent,
    AddPersonalComponent,
    ListPersonalComponent,
    LoginComponent,
    RegisterComponent,
    NotasCursoComponent,
    HasRoleDirective,
    LoadingOverlayComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
