export type Cargo = 'DIRECTOR' | 'VICEDIRECTOR' | 'SECRETARIO' | 'PRECEPTOR' | 'DOCENTE' | 'ADMINISTRATIVO';

export interface Personal {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  cargo: Cargo;
}

// Lo que se manda al hacer POST /personal (coincide con PersonalAltaDTO del backend)
export interface PersonalAlta {
  nombre: string;
  apellido: string;
  email: string;
  cargo: Cargo;
}
