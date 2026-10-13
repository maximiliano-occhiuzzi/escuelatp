# Escuela TP — backend (Spring Cloud)

Sistema de gestión de un colegio: **alumnos, cursos, notas, personal y usuarios con roles**.

Son tres repos: `escuelatp` (este: backend), `escuelatp-properties` (configuración que sirve el Config Server) y `escuelatp-front` (Angular).

## Índice
1. [Tecnologías](#1-tecnologías)
2. [Arquitectura](#2-arquitectura)
3. [Cumplimiento de la consigna](#3-cumplimiento-de-la-consigna)
4. [Estructura de cada microservicio](#4-estructura-de-cada-microservicio)
5. [Modelo de datos](#5-modelo-de-datos)
6. [Roles y permisos](#6-roles-y-permisos)
7. [Endpoints](#7-endpoints)
8. [Flujos paso a paso](#8-flujos-paso-a-paso)
9. [Repo de configuración (escuelatp-properties)](#9-repo-de-configuración-escuelatp-properties)
10. [Cómo levantarlo en desarrollo](#10-cómo-levantarlo-en-desarrollo)
11. [Producción con Docker Compose](#11-producción-con-docker-compose)
12. [Swagger](#12-swagger--openapi)
13. [Cómo probarlo en Postman](#13-cómo-probarlo-en-postman)
14. [Subir el proyecto a GitHub](#14-subir-el-proyecto-a-github)
15. [Problemas frecuentes](#15-problemas-frecuentes)
16. [Limitaciones conocidas](#16-limitaciones-conocidas)

---

## 1. Tecnologías

| Tema | Qué se usa |
|---|---|
| Lenguaje / framework | Java 17, Spring Boot 4.0.7, Spring Cloud 2025.1.2 |
| Descubrimiento | Eureka (`registry`) |
| Configuración centralizada | Spring Cloud Config Server (backend Git) |
| Puerta de entrada | Spring Cloud Gateway **MVC** (servlet, no reactivo) |
| Monitoreo | Spring Boot Admin (`AdminServer`) |
| Seguridad | Spring Security + JWT (HS256, Nimbus) + refresh tokens |
| API | Spring Web (REST), validaciones con `jakarta.validation` |
| Persistencia | Spring Data JPA / Hibernate. **H2** en desarrollo, **MySQL** en producción |
| Mensajería asincrónica | RabbitMQ (Spring AMQP) |
| Comunicación sincrónica | OpenFeign (Alumno → Cursos) |
| Documentación | springdoc OpenAPI / Swagger UI 3.1.0 |
| Contenedores | Docker + Docker Compose |

## 2. Arquitectura

```
Front (Angular :4200)
        │
        ▼
 Gateway :8081 ── valida el JWT · aplica permisos por rol · agrega X-Gateway-Secret y X-User-*
   │        │          │            │
 Auth     Alumno     Cursos     Administracion      (cada uno con SU base de datos)
 :8086    :8084      :8083       :8085
            │  ▲         ▲
            │  └─ Feign ─┘      Alumno ──consulta──▶ Cursos   (sincrónico: ¿existe el curso? ¿hay cupo?)
            └──── RabbitMQ ───▶  Alumno ──evento───▶ Cursos   (asincrónico: alta y baja de alumno)

 registry (Eureka) :8761 · configServer :8888 · AdminServer :8090 · RabbitMQ :5672 / panel :15672
```

| Servicio | Carpeta | `spring.application.name` | Puerto | Qué hace |
|---|---|---|---|---|
| Registry | `registry` | `eurekaserver` | 8761 | Eureka: los servicios se registran y se encuentran por nombre |
| Config Server | `configServer` | `configserver` | 8888 | Entrega a cada servicio su yml desde el repo `escuelatp-properties` |
| Admin Server | `AdminServer` | `adminserver` | 8090 | Panel de monitoreo de los servicios |
| Gateway | `Gateway` | `gateway` | 8081 | Única entrada para el front. Seguridad y ruteo |
| Auth | `Auth` | `auth` | 8086 | Login, refresh, logout, alta de usuarios. Emite los JWT |
| Alumnos | `Alumno` | `modulo-alumnos` | 8084 | Alta, consulta y baja lógica de alumnos. **Productor** RabbitMQ |
| Cursos | `Cursos` | `modulo-cursos` | 8083 | Cursos y notas. **Consumidor** RabbitMQ |
| Administración | `Administracion` | `modulo-administracion` | 8085 | Personal (docentes, directivos, administrativos) |

**Seguridad en capas**
1. El **Gateway** valida el JWT y decide qué rol puede usar cada ruta.
2. El Gateway borra cualquier header `X-User-*` que mande el cliente y agrega los propios (`X-User-Username`, `X-User-Roles`, `X-User-Personal-Id`) desde el JWT.
3. El Gateway agrega `X-Gateway-Secret`. Cada microservicio tiene un `GatewayInterceptor` que responde **403** a todo pedido sin ese header: nadie puede saltearse el Gateway.
4. **Cursos** controla la regla "de fila": el docente solo toca *sus* cursos (con `X-User-Personal-Id`).

## 3. Cumplimiento de la consigna

### Requerimientos técnicos
| # | Requerimiento | Dónde está |
|---|---|---|
| 1 | Arquitectura Spring Cloud completa | `registry`, `configServer`, `Gateway`, `AdminServer`, Feign (Alumno → Cursos) |
| 2 | Spring Security | `Gateway/.../GatewaySecurityConfig`, `Auth/.../SecurityConfig` |
| 3 | Spring Web, API REST | Todos los `controller` (verbos, códigos HTTP y recursos REST) |
| 4 | Manejo de errores con Spring | `exception/GlobalExceptionHandler` (`@RestControllerAdvice`) en cada servicio |
| 5 | Comunicación asincrónica con RabbitMQ | `Alumno/.../RabbitMQConfig` (productor) y `Cursos/.../CursoListener` (consumidor) |
| 6 | Roles | `Auth/.../Rol` (ROOT, DIRECTOR, PRECEPTOR, DOCENTE) + reglas del Gateway |
| 7 | JPA con Hibernate | Entidades en cada `model/` |
| 8 | H2 en desarrollo / MySQL en producción | `*-dev.yml` (H2) y `*-docker.yml` (MySQL) en el repo de properties |
| 9 | Swagger / OpenAPI en todos los microservicios | `OpenApiConfig` + `@Tag`/`@Operation` en los controllers |
| 10 | Front en Angular con framework CSS | Repo `escuelatp-front` (Bootstrap) |
| 11 | Filtrar pantallas y componentes por rol | Front (el login devuelve el rol) |
| 12 | Dockerizado con docker compose | `docker-compose.yml` + un `Dockerfile` por servicio |

### Criterios de evaluación
| Criterio | Cómo se cumple |
|---|---|
| Arquitectura de 3 capas | `controller` → `service` (interface + implementación) → `repository`, en los cuatro microservicios |
| Controllers con DTO | Ningún controller recibe ni devuelve entidades: `*AltaDTO` entra, `*DTO` sale. Los DTO de alta implementan `IMapper<E>` (`mapperTo()`) |
| Entidades bien relacionadas | `Curso` 1—N `Alumno`, `Curso` 1—N `Nota` (cascade + orphanRemoval), `RefreshToken` N—1 `Usuario`. Entre microservicios solo se guardan ids (cada uno tiene su base) |
| Restricciones por rol | Ver [sección 6](#6-roles-y-permisos) |
| La maqueta utilizada | Front en Angular + Bootstrap (repo `escuelatp-front`) |
| RabbitMQ para lo asincrónico | Eventos de alta y baja de alumno (sección 8) |

## 4. Estructura de cada microservicio

Paquete base `com.app`. Todos siguen el mismo esquema:

```
com.app
├── controller/   REST: recibe DTO, devuelve DTO. Solo delega en el service
├── service/      interface + implementación con la lógica de negocio y las reglas
├── repository/   interfaces Spring Data JPA
├── model/        entidades JPA
├── dto/          DTO de entrada (*AltaDTO) y salida (*DTO)
├── mapper/       IMapper<E>: convierte un DTO en entidad (mapperTo())
├── exception/    excepciones de negocio + GlobalExceptionHandler + ApiError
└── config/       GatewayInterceptor, WebMvcConfig, OpenApiConfig, DataInitializer...
```
Particularidades:
- **Auth**: `security/` (JwtService, RefreshTokenService, SecurityConfig).
- **Alumno**: `client/` (Feign hacia Cursos), `senders/` (eventos que publica), `config/RabbitMQConfig`, `config/FeignConfig`.
- **Cursos**: `listener/` (`CursoListener` + copias de los eventos), `security/UsuarioActual` (lee los headers del Gateway), `enums/DiaSemana`.

## 5. Modelo de datos

```
AUTH                              ALUMNO
Usuario (usuarios)                Alumno (alumnos)
 ├ id, username (único)            ├ id, nombre, apellido
 ├ password (BCrypt)               ├ dni (único), email
 ├ rol: ROOT|DIRECTOR|             ├ estado: ASIGNADO | SINASIGNACION | DESHABILITADO
 │      PRECEPTOR|DOCENTE          └ cursoId  ← solo el id (el curso vive en Cursos)
 └ personalId (solo DOCENTE)
RefreshToken (refresh_tokens)
 └ N──1 Usuario

CURSOS                                              ADMINISTRACION
Curso (cursos)                                      Personal (personal)
 ├ id, materia                                       ├ id, nombre, apellido
 ├ diaSemana (LUNES..SABADO)                         ├ email (único)
 ├ horaInicio, horaFin                               └ cargo: DIRECTOR | VICEDIRECTOR | SECRETARIO |
 ├ cupoMaximo, cicloLectivo                                    PRECEPTOR | DOCENTE | ADMINISTRATIVO
 ├ docenteId  ← id de Personal
 ├ 1──N Alumno (alumnos): referencia local por id, la carga RabbitMQ
 └ 1──N Nota   (notas)
Nota: id, alumnoId, valor (1..10), descripcion, fecha, N──1 Curso
```
Por qué hay ids sueltos: cada microservicio tiene su propia base, así que no puede haber `@ManyToOne` entre ellos. Se referencian por id (`cursoId`, `docenteId`, `personalId`, `alumnoId`).

## 6. Roles y permisos

| Acción | ROOT | DIRECTOR | PRECEPTOR | DOCENTE |
|---|:-:|:-:|:-:|:-:|
| Crear usuarios con perfil | ✅ | ❌ | ❌ | ❌ |
| Crear cursos | ✅ | ❌ | ✅ | ❌ |
| Ver cursos | todos | todos | todos | **solo los suyos** |
| Alta de alumno | ❌ | ❌ | ✅ | ❌ |
| Deshabilitar alumno | ❌ | ❌ | ✅ | ❌ |
| Ver alumnos | ✅ | ✅ | ✅ | ✅ |
| Ver notas | ❌ | todas | ❌ | solo de sus cursos |
| Agregar / modificar / eliminar notas | ❌ | ❌ (solo lectura) | ❌ | solo en sus cursos |
| Alta / modificación de personal | ✅ | ❌ | ✅ | ❌ |
| Ver personal | ✅ | ✅ | ✅ | ❌ |

Ejemplos de la consigna: *"el docente solo ve los cursos y agrega, modifica y elimina notas en los suyos"*, *"el director ve todos los cursos y las notas pero no las modifica"*, *"ROOT crea usuarios con perfiles y cursos pero no agrega alumnos"*, *"el preceptor agrega y deshabilita alumnos y crea cursos"*.

## 7. Endpoints

Todos se llaman **contra el Gateway** (`http://localhost:8081`). Todos exigen `Authorization: Bearer <token>` salvo los públicos de Auth.

| Servicio | Método y ruta | Roles | Respuesta |
|---|---|---|---|
| Auth | `POST /auth/login` | público | 200 con `token`, `refreshToken`, `rol`… |
| Auth | `POST /auth/refresh` | público (usa el refresh token) | 200 con tokens nuevos |
| Auth | `POST /auth/logout` | público (usa el refresh token) | 200 |
| Auth | `POST /auth/register` | ROOT | 201 |
| Alumnos | `PUT /alumnos/add` | PRECEPTOR | 201 sin cuerpo |
| Alumnos | `GET /alumnos` | cualquier usuario logueado | 200 lista |
| Alumnos | `GET /alumnos/{id}` | cualquier usuario logueado | 200 / 404 |
| Alumnos | `PATCH /alumnos/{id}/deshabilitar` | PRECEPTOR | 200 con el alumno |
| Alumnos | `PATCH /alumnos/{id}/curso` (body `{"cursoId": 3}`) | PRECEPTOR | 200 con el alumno; 400 curso inexistente; 409 sin cupo, ya está en ese curso o está deshabilitado |
| Alumnos | `PATCH /alumnos/{id}/habilitar` | PRECEPTOR | 200 con el alumno (vuelve a SINASIGNACION); 409 si no estaba deshabilitado |
| Cursos | `POST /cursos` | ROOT, PRECEPTOR | 201 |
| Cursos | `GET /cursos` | logueado (DOCENTE: solo los suyos) | 200 lista |
| Cursos | `GET /cursos/{id}` | logueado (DOCENTE: solo si es suyo) | 200 / 403 / 404 |
| Notas | `GET /cursos/{id}/notas` | DOCENTE (sus cursos), DIRECTOR | 200 lista |
| Notas | `POST /cursos/{id}/notas` | DOCENTE (su curso) | 201 |
| Notas | `PUT /cursos/{id}/notas/{notaId}` | DOCENTE (su curso) | 200 |
| Notas | `DELETE /cursos/{id}/notas/{notaId}` | DOCENTE (su curso) | 204 |
| Personal | `POST /personal` | ROOT, PRECEPTOR | 201 |
| Personal | `PUT /personal/{id}` | ROOT, PRECEPTOR | 200 |
| Personal | `GET /personal` y `GET /personal/{id}` | ROOT, PRECEPTOR, DIRECTOR | 200 |

**Códigos de error** (siempre con el mismo formato `{ timestamp, status, error, message, path, details }`):

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos (`details` lista campo por campo), JSON mal formado, curso inexistente al dar de alta un alumno, alumno no inscripto al cargar una nota |
| 401 | Credenciales o token inválido/vencido |
| 403 | Rol sin permiso, docente que no es el del curso, o pedido que no pasó por el Gateway |
| 404 | El recurso no existe |
| 409 | DNI o email duplicado, usuario ya existente, persona del personal que ya tiene usuario, curso sin cupo, alumno ya deshabilitado |
| 503 | Cursos no responde cuando Alumno lo consulta por Feign |

## 8. Flujos paso a paso

**Login y token**
1. `POST /auth/login` → Auth valida con BCrypt y devuelve un **access token** (JWT, 15 min) y un **refresh token** (guardado en la base de Auth, 7 días).
2. El JWT lleva `sub` (username), `roles` (por ejemplo `["ROLE_DOCENTE"]`) y, para docentes, `personalId`.
3. Cuando el access token vence, el front pide uno nuevo con `POST /auth/refresh` (el refresh token se rota). `POST /auth/logout` revoca el refresh token.

**Alta de alumno con curso** (`PUT /alumnos/add`, PRECEPTOR)
1. El Gateway valida el JWT y el rol, y reenvía a Alumno.
2. Alumno convierte el DTO en entidad (`mapperTo()`): con `cursoId` queda `ASIGNADO`, sin él `SINASIGNACION`.
3. Verifica que el DNI no exista (409) y, si trae curso, le **consulta a Cursos por Feign** si existe (400) y si tiene cupo (409).
4. Guarda el alumno y, **después del commit**, publica `AlumnoEvent {idAlumno, idCurso}` en `alumno.exchange` con la routing key `alumno.alta.routingkey`.
5. RabbitMQ lo deja en `alumno.alta.queue`. En Cursos, `CursoListener` lo recibe, comprueba cupo y duplicados y agrega el alumno al curso.
6. Responde **201** apenas guardó, sin esperar a Cursos.

**Baja lógica** (`PATCH /alumnos/{id}/deshabilitar`, PRECEPTOR)
1. El alumno pasa a `DESHABILITADO` y se le quita el `cursoId` (el registro **no se borra**).
2. Si estaba en un curso, se publica `AlumnoBajaEvent` (routing key `alumno.baja.routingkey`, cola `alumno.baja.queue`).
3. Cursos lo saca de la lista del curso y se libera el cupo.

**Notas** (DOCENTE)
1. El Gateway deja pasar solo al rol correcto según el método.
2. `NotaServiceImpl` vuelve a exigir rol DOCENTE y que `X-User-Personal-Id` coincida con el `docenteId` del curso (si no, 403).
3. Para agregar, el alumno tiene que estar inscripto en el curso (400 si no). El valor va de 1 a 10.

## 9. Repo de configuración (escuelatp-properties)

El Config Server lo clona de `https://github.com/maximiliano-occhiuzzi/escuelatp-properties` (rama `main`, ver `configServer/src/main/resources/application.yml`). Cada servicio pide su archivo según su `spring.application.name` y el perfil activo.

| Archivo | Para quién | Contenido |
|---|---|---|
| `application.yml` | **todos** | `jwt.secret` (`${JWT_SECRET}`), duración de los tokens, `gateway.internal-secret`, `openapi.gateway-url` |
| `gateway-dev.yml` / `gateway-docker.yml` | Gateway | Rutas hacia los servicios (`lb://...`) y el filtro `SetRequestHeader=X-Gateway-Secret` |
| `auth-dev.yml` / `-docker.yml` | Auth | Puerto 8086, base de datos |
| `modulo-alumnos-dev.yml` / `-docker.yml` | Alumno | Puerto 8084, base de datos, RabbitMQ |
| `modulo-cursos-dev.yml` / `-docker.yml` | Cursos | Puerto 8083, base de datos, RabbitMQ (sin reintentos infinitos) |
| `modulo-administracion-dev.yml` / `-docker.yml` | Administración | Puerto 8085, base de datos |
| `modulo-registry.yml` | — | No lo usa nadie (el registry tiene su propio `application.yml`); se puede borrar |

- Perfil **`dev`** (por defecto): H2 en archivo, RabbitMQ en `localhost`, Eureka en `localhost`.
- Perfil **`docker`**: MySQL, RabbitMQ y registry por nombre de servicio; las claves salen de variables de entorno.
- Los endpoints de actuator expuestos son solo `health,info,metrics,loggers` (no `*`: `heapdump` o `env` pueden filtrar secretos).

**Variables de entorno**

| Variable | Dónde | Para qué |
|---|---|---|
| `JWT_SECRET` | **Solo Auth y Gateway** (mismo valor, en **Base64**) | Firmar y validar los JWT. Generarlo con `openssl rand -base64 48` |
| `MYSQL_ROOT_PASSWORD` | Auth, Alumno, Cursos, Administración (docker) | Conexión a MySQL |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | Alumno, Cursos (docker) | Conexión a RabbitMQ |

`gateway.internal-secret` está en texto plano en `application.yml` (aprobado por la cátedra), así que no necesita variable de entorno.

## 10. Cómo levantarlo en desarrollo

**Requisitos:** Java 17, Maven (o el `mvnw` de cada módulo), Docker (para RabbitMQ), acceso a internet para que el Config Server clone el repo.

1. Subir `escuelatp-properties` a GitHub y confirmar que la URL de `configServer/.../application.yml` es la tuya.
2. RabbitMQ:
   ```
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
   ```
   Panel: http://localhost:15672 (`guest` / `guest`). La próxima vez: `docker start rabbitmq`.
3. Definir `JWT_SECRET` en las *Run Configurations* de **Auth** y **Gateway** (Environment → Add), con el mismo valor.
4. Levantar en este orden, esperando que cada uno termine de arrancar:

   | # | Servicio | Comprobación |
   |---|---|---|
   | 1 | registry | http://localhost:8761 |
   | 2 | configServer | http://localhost:8888/application/default (devuelve un JSON) |
   | 3 | AdminServer | http://localhost:8090 |
   | 4 | Auth | aparece `AUTH` en Eureka |
   | 5 | Cursos | aparece `MODULO-CURSOS` |
   | 6 | Alumno | aparece `MODULO-ALUMNOS` |
   | 7 | Administracion | aparece `MODULO-ADMINISTRACION` |
   | 8 | Gateway | aparece `GATEWAY` |

5. Las bases H2 de dev son **en archivo**: se guardan en la carpeta `data/` del módulo y sobreviven a los reinicios. Para empezar de cero, parar los servicios y borrar esa carpeta. Consola H2 de cada servicio: `http://localhost:<puerto>/h2-console`.

## 11. Producción con Docker Compose

1. `escuelatp-front` tiene que estar como carpeta **hermana** de `escuelatp` (el compose la construye).
2. Copiar `.env.example` a `.env` y completarlo. **El `.env` no se sube a Git** (está en el `.gitignore`).
3. Desde la carpeta `escuelatp`:
   ```
   docker compose up --build
   ```
4. Accesos: front http://localhost:4200 · Gateway http://localhost:8081 · Eureka http://localhost:8761 · Admin http://localhost:8090 · RabbitMQ http://localhost:15672

Cómo arranca: MySQL y RabbitMQ esperan a estar sanos; el Config Server clona el repo y recién entonces arrancan los microservicios (`depends_on` con `healthcheck`). Cada servicio usa el perfil `docker`, y las bases se crean solas (`createDatabaseIfNotExist`) en un único contenedor MySQL (`authdb`, `alumnosdb`, `cursosdb`, `administraciondb`). El puerto 8888 del Config Server no se publica a propósito.

Comandos útiles: `docker compose logs -f cursos` · `docker compose down` · `docker compose down -v` (borra también los datos de MySQL).

## 12. Swagger / OpenAPI

| Servicio | URL |
|---|---|
| Auth | http://localhost:8086/swagger-ui.html |
| Cursos y notas | http://localhost:8083/swagger-ui.html |
| Alumno | http://localhost:8084/swagger-ui.html |
| Administración | http://localhost:8085/swagger-ui.html |

Para probar endpoints protegidos: hacer `POST /auth/login`, copiar el `token` y pegarlo en el botón **Authorize**. Los *Try it out* se mandan al **Gateway** (`openapi.gateway-url`), porque los servicios rechazan lo que no pase por él.

## 13. Cómo probarlo en Postman

Todo contra `http://localhost:8081`. En *Authorization* elegir **Bearer Token** (no "JWT Bearer") y pegar el `token` del login.

**Login** → `POST /auth/login`
```json
{ "username": "preceptor", "password": "preceptor123" }
```

**Usuarios de prueba** (los crea Auth al arrancar):

| Usuario | Contraseña | Rol |
|---|---|---|
| root | root123 | ROOT |
| director | director123 | DIRECTOR |
| preceptor | preceptor123 | PRECEPTOR |
| docente | docente123 | DOCENTE (es el Personal id 1) |

Al arrancar también se crean: el Personal id 1 (Ana López, DOCENTE) y id 2 (Laura Gómez, DIRECTOR), y el curso "Matemática" (lunes 08:00–10:00) asignado al docente 1.

**Crear usuario** (como ROOT) → `POST /auth/register`
```json
{ "username": "maria", "password": "maria123", "rol": "DOCENTE", "personalId": 1 }
```
**Crear curso** (ROOT o PRECEPTOR) → `POST /cursos`
```json
{ "materia": "Lengua", "diaSemana": "MARTES", "horaInicio": "10:00", "horaFin": "12:00",
  "cupoMaximo": 25, "cicloLectivo": 2026, "docenteId": 1 }
```
**Alta de alumno** (PRECEPTOR) → `PUT /alumnos/add`
```json
{ "nombre": "Juan", "apellido": "Perez", "dni": "30111222", "email": "juan@mail.com", "cursoId": 1 }
```
**Deshabilitar alumno** (PRECEPTOR) → `PATCH /alumnos/1/deshabilitar`

**Cargar nota** (DOCENTE) → `POST /cursos/1/notas`
```json
{ "alumnoId": 1, "valor": 8.5, "descripcion": "Parcial 1" }
```
**Alta de personal** (ROOT o PRECEPTOR) → `POST /personal`
```json
{ "nombre": "Pedro", "apellido": "Suarez", "email": "pedro@escuela.com", "cargo": "DOCENTE" }
```

**Casos que tienen que fallar** (para mostrar las restricciones):

| Pedido | Resultado |
|---|---|
| ROOT hace `PUT /alumnos/add` | 403 |
| DIRECTOR hace `POST /cursos/1/notas` | 403 (solo lectura) |
| DOCENTE hace `GET /cursos/2/notas` de un curso que no es suyo | 403 |
| DOCENTE hace `POST /cursos` | 403 |
| Alta de alumno con DNI repetido | 409 |
| Alta de alumno con un `cursoId` que no existe | 400 |
| Alta de alumno en un curso sin cupo | 409 |
| Nota para un alumno que no está en el curso | 400 |
| Pegarle directo a `localhost:8084/alumnos` (sin el Gateway) | 403 |
| Cualquier ruta sin token | 401 |

**Ver los mensajes de RabbitMQ:** en http://localhost:15672 → *Queues* → `alumno.alta.queue` / `alumno.baja.queue`. Con Cursos andando, los mensajes se consumen al instante (Ready vuelve a 0): se ve el pico en *incoming / deliver / ack*. El `CursoListener` también escribe cada evento en su log.

## 14. Subir el proyecto a GitHub

Son dos repos separados. Con los repos vacíos ya creados en GitHub:

```bash
# backend
cd escuelatp
git init && git add . && git commit -m "Backend inicial"
git branch -M main
git remote add origin https://github.com/maximiliano-occhiuzzi/escuelatp.git
git push -u origin main

# properties
cd ../escuelatp-properties
git init && git add . && git commit -m "Properties iniciales"
git branch -M main
git remote add origin https://github.com/maximiliano-occhiuzzi/escuelatp-properties.git
git push -u origin main
```
Verificar que NO se suban `.env`, carpetas `data/` ni `target/` (el `.gitignore` ya los excluye). Si cambiás un properties, hay que hacer push y **reiniciar** el servicio afectado (no se recarga solo).

## 15. Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Unable to determine Dialect without JDBC metadata` al arrancar un servicio | El servicio no está leyendo las propiedades del Config Server. Comprobá `http://localhost:8888/<servicio>/dev` y que el pom **no** tenga `spring-cloud-starter-bootstrap` (con esa dependencia Spring ignora `spring.config.import`). |
| `Could not resolve placeholder 'JWT_SECRET'` | Falta la variable de entorno en la Run Configuration de **Auth** o **Gateway** |
| Auth/Gateway fallan con un error de Base64 | `JWT_SECRET` no está en Base64. Generarlo con `openssl rand -base64 48` |
| Un servicio no arranca: no encuentra el Config Server | Levantar primero `configServer` y esperar a que responda |
| El Config Server falla al clonar el repo | URL equivocada, rama distinta de `main`, repo privado, o sin internet |
| Cambié un properties y no se nota | Hacer push al repo y **reiniciar** el servicio |
| 403 "Acceso denegado: las peticiones deben pasar por el Gateway" | Se está llamando directo al puerto del servicio. Usar siempre `:8081` |
| 401 en Postman con token válido | Elegir **Bearer Token** (no "JWT Bearer"). El access token dura 15 min: volver a loguearse |
| 403 con un usuario logueado | Ese rol no tiene permiso para esa ruta (ver sección 6) |
| 503 al dar de alta un alumno con curso | Cursos no está levantado o no se registró todavía en Eureka |
| Los alumnos no aparecen inscriptos en el curso | RabbitMQ apagado, o Cursos caído. Mirar el panel y el log de Cursos |
| `Connection refused` a RabbitMQ | El contenedor no está corriendo: `docker start rabbitmq` |
| Docker: `dockerDesktopLinuxEngine ... 500` | Docker Desktop todavía no arrancó (esperar "Engine running" o reiniciar la PC) |
| `Port 80xx already in use` | Hay otra instancia del servicio corriendo. Pararla |
| Error de H2 "Database may be already in use" | Hay dos instancias del mismo servicio usando el mismo archivo. Dejar una sola |
| Quiero empezar con datos limpios | Parar todo y borrar la carpeta `data/` de cada módulo |
| Compose: el `configserver` queda `unhealthy` | `docker compose logs configserver`: suele ser la URL del repo o falta de internet |

## 16. Limitaciones conocidas

- Si RabbitMQ está caído al guardar, el alumno se guarda igual y se loguea un warning, pero el aviso a Cursos no se reenvía. Si lo que cae es Cursos, el mensaje espera en la cola (durable) y se procesa cuando vuelve.
- No hay cola de mensajes muertos: un evento inválido se descarta (se loguea).
- No se valida que el `docenteId` (curso) o el `personalId` (usuario) exista en Administración: son referencias por id entre microservicios.
- La clave `gateway.internal-secret` está en texto plano (aprobado por la cátedra). Para un entorno real: Kubernetes Secret, `{cipher}` del Config Server o Vault.
- El access token no se puede revocar (es la naturaleza de un JWT): por eso dura solo 15 minutos y lo que sí se revoca es el refresh token.
- El código se generó sin poder compilarlo con Maven. Si al abrirlo aparece un error de compilación o de arranque, hay que corregirlo.
