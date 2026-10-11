import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';

// Convierte un error HTTP en un texto para mostrar. Usa el mensaje que manda
// el backend (formato ApiError) cuando existe.
export function mensajeError(err: HttpErrorResponse, porDefecto: string): string {
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor. Revisá que el backend esté levantado.';
  }
  if (err.status === 403) {
    return 'No tenés permiso para realizar esta acción.';
  }
  const api = err.error as ApiError | null;
  if (api && typeof api === 'object' && api.message) {
    const detalles = api.details?.length ? ' (' + api.details.join('; ') + ')' : '';
    return api.message + detalles;
  }
  return porDefecto;
}
