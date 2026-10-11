import { Curso, DIAS } from '../models/curso.model';

export function nombreDia(dia: string): string {
  return DIAS.find(d => d.valor === dia)?.nombre ?? dia;
}

// "08:00:00" -> "08:00"
export function hora(h: string): string {
  return (h ?? '').substring(0, 5);
}

// Texto corto para selects y listados: "Matemática · Lunes 08:00-10:00"
export function etiquetaCurso(c: Curso): string {
  return `${c.materia} · ${nombreDia(c.diaSemana)} ${hora(c.horaInicio)}-${hora(c.horaFin)} (${c.cicloLectivo})`;
}
