import { Component, OnInit } from '@angular/core';
import { Rol } from '../../models/auth.model';
import { HttpErrorResponse } from '@angular/common/http';
import { mensajeError } from '../../shared/mensaje-error';
import { Personal } from '../../models/personal.model';
import { ServiciosService } from '../../service/servicios.service';

@Component({
  selector: 'app-list-personal',
  templateUrl: './list-personal.component.html',
  styleUrls: ['./list-personal.component.css']
})
export class ListPersonalComponent implements OnInit {

  personal: Personal[] = [];
  cargando = true;
  error: string | null = null;
  filtro = '';

  constructor(
    private serviciosService: ServiciosService
  ) { }

  readonly rolesAlta: Rol[] = ['ROOT', 'PRECEPTOR'];

  get personalFiltrados(): Personal[] {
    const f = this.filtro.trim().toLowerCase();
    if (!f) { return this.personal; }
    return this.personal.filter(p =>
      [p.nombre, p.apellido, p.email, p.cargo].some(v => (v ?? '').toString().toLowerCase().includes(f)));
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = null;

    this.serviciosService.listarPersonal().subscribe({
      next: (data) => {
        this.personal = data;
        this.cargando = false;
      },
      error: (err: HttpErrorResponse) => {
        this.error = mensajeError(err, 'No se pudo obtener el listado.');
        this.cargando = false;
      }
    });
  }
}
