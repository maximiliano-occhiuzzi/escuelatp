export interface Nota {
  id: number;
  cursoId: number;
  alumnoId: number;
  valor: number;
  descripcion?: string | null;
  fecha: string; // yyyy-MM-dd
}

export interface NotaAlta {
  alumnoId: number;
  valor: number;
  descripcion?: string | null;
  fecha?: string | null;
}

export interface NotaModificar {
  valor: number;
  descripcion?: string | null;
  fecha?: string | null;
}
