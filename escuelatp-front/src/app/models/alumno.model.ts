export type EstadoAlumno = 'ASIGNADO' | 'SINASIGNACION' | 'DESHABILITADO';

export interface Alumno {
  id: number;
  nombre: string;
  apellido: string;
  dni: string;
  email: string;
  estado: EstadoAlumno;
  cursoId?: number | null;
}

// Coincide con AlumnoAltaDTO del backend.
export interface AlumnoAlta {
  nombre: string;
  apellido: string;
  dni: string;
  email: string;
  cursoId?: number | null;
}
