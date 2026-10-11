export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO';

export const DIAS: { valor: DiaSemana; nombre: string }[] = [
  { valor: 'LUNES', nombre: 'Lunes' },
  { valor: 'MARTES', nombre: 'Martes' },
  { valor: 'MIERCOLES', nombre: 'Miércoles' },
  { valor: 'JUEVES', nombre: 'Jueves' },
  { valor: 'VIERNES', nombre: 'Viernes' },
  { valor: 'SABADO', nombre: 'Sábado' }
];

// Coincide con CursoDTO del backend.
export interface Curso {
  id: number;
  materia: string;
  diaSemana: DiaSemana;
  horaInicio: string; // "HH:mm:ss"
  horaFin: string;
  cupoMaximo: number;
  cicloLectivo: number;
  docenteId?: number | null;
  inscriptos: number;
  alumnosIds: number[];
}

// Coincide con CursoAltaDTO del backend.
export interface CursoAlta {
  materia: string;
  diaSemana: DiaSemana;
  horaInicio: string; // "HH:mm"
  horaFin: string;
  cupoMaximo: number;
  cicloLectivo: number;
  docenteId?: number | null;
}
