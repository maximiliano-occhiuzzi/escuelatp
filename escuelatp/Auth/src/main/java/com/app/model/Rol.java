package com.app.model;

/**
 * Roles del sistema (se guardan en el JWT con el prefijo ROLE_).
 *
 *  ROOT      -> crea usuarios con perfil y cursos. No gestiona alumnos.
 *  DIRECTOR  -> ve todos los cursos y las notas, sin poder modificarlas.
 *  PRECEPTOR -> alta y baja logica de alumnos, crea cursos, gestiona personal.
 *  DOCENTE   -> ve sus cursos y agrega/modifica/elimina notas en ellos.
 */
public enum Rol {
    ROOT,
    DIRECTOR,
    PRECEPTOR,
    DOCENTE
}
