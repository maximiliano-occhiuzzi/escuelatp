# Escuela TP - Cómo levantarlo

## Repositorios

| Repo | Contenido |
|---|---|
| [escuelatp](https://github.com/maximiliano-occhiuzzi/escuelatp) | Backend (microservicios) y `docker-compose.yml` |
| [escuelatp-front](https://github.com/maximiliano-occhiuzzi/escuelatp-front) | Front Angular + Bootstrap |
| [escuelatp-properties](https://github.com/maximiliano-occhiuzzi/escuelatp-properties) | Configuración que sirve el Config Server (dev con H2, docker con MySQL) |

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| root | root123 | Root |
| director | director123 | Director |
| preceptor | preceptor123 | Preceptor |
| docente | docente123 | Docente |

## Opción 1: con Docker (recomendada)

Solo hace falta **Docker Desktop instalado y abierto** (esperar a que diga *Engine running*). No hay que compilar nada ni crear un `.env`.

1. Clonar el repo y entrar a la carpeta del compose:

   ```
   git clone https://github.com/maximiliano-occhiuzzi/escuelatp
   cd escuelatp/escuelatp
   ```

2. Levantar el sistema:

   ```
   docker compose up -d
   ```

3. Docker baja las imágenes `maxmegafuego/escuelatp-*` desde Docker Hub (backend y front) y trae valores por defecto para todas las claves, por eso no se necesita `.env`. Cada microservicio con base de datos tiene **su propio contenedor MySQL**.
4. Esperar 2-3 minutos a que todos los servicios se registren (el primer arranque es lento) y abrir:

   | Qué | URL |
   |---|---|
   | Front | http://localhost:4200 |
   | Eureka (servicios registrados) | http://localhost:8761 |
   | RabbitMQ | http://localhost:15672 (escuela / escuela) |
   | Swagger Auth | http://localhost:8086/swagger-ui.html |
   | Swagger Alumno | http://localhost:8084/swagger-ui.html |
   | Swagger Cursos | http://localhost:8083/swagger-ui.html |
   | Swagger Administración | http://localhost:8085/swagger-ui.html |

   En Eureka tienen que aparecer: `AUTH`, `GATEWAY`, `ADMINSERVER`, `MODULO-ALUMNOS`, `MODULO-CURSOS` y `MODULO-ADMINISTRACION`.

5. Para apagarlo: `docker compose down` (con `-v` también borra los datos de las bases).

### Bases de datos (una por microservicio)

| Servicio | Contenedor | Base | Usuario | Contraseña | Puerto en tu PC |
|---|---|---|---|---|---|
| Auth | `auth-db` | `auth_database` | `auth_user` | `auth_pass` | 3307 |
| Alumno | `alumno-db` | `alumnos_database` | `alumno_user` | `alumno_pass` | 3308 |
| Cursos | `cursos-db` | `cursos_database` | `cursos_user` | `cursos_pass` | 3309 |
| Administración | `administracion-db` | `administracion_database` | `administracion_user` | `admin_pass` | 3310 |

Para mirarlas desde MySQL Workbench o DBeaver: host `localhost`, el puerto y el usuario de la tabla. Cada una tiene además un usuario `root` (contraseñas `root_auth_pass`, `root_alumno_pass`, `root_cursos_pass`, `root_admin_pass`). Son claves de demostración.

### Desarrollo (H2) y producción (MySQL)

- **Local / dev (por defecto):** el perfil de Maven `dev` está activo por defecto e incluye H2 y su consola. Los datos se guardan en archivo (`./data`).
- **Docker / prod:** los Dockerfiles compilan con `-Pprod`, que no incluye H2: solo MySQL.
- La configuración de cada perfil vive en el repo `escuelatp-properties` (`*-dev.yml` y `*-docker.yml`).

## Opción 2: con Docker, compilando desde el código fuente

Construye las imágenes en la propia máquina. El front se baja y se construye solo desde su repo de GitHub (hace falta internet). Se corre desde `escuelatp/escuelatp`:

```
docker compose -f docker-compose.yml -f docker-compose.build.yml up --build
```

Opcional: copiar `.env.example` como `.env` para cambiar claves. El `.env` no se sube a Git (está en el `.gitignore`).

## Opción 3: sin Docker (Eclipse / IDE)

Se usa el perfil `dev` con H2. Hace falta un RabbitMQ local (por ejemplo `docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management`).

Agregar la variable de entorno `JWT_SECRET` en **Auth** y en **Gateway**. Tiene que ser **el mismo valor** en los dos, si no, el token no valida y no funciona el login. Orden de arranque: Registry, Config Server, Admin Server, Auth, Alumno, Cursos, Administración y Gateway. Para el front: `npm install` y `npm start` en el repo `escuelatp-front`.

## Generar un JWT_SECRET en Windows

`openssl` no existe en Windows. En PowerShell:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```

## Si algo no anda

| Problema | Solución |
|---|---|
| `failed to connect to the docker API ... dockerDesktopLinuxEngine` | Docker Desktop no está abierto. Abrirlo, esperar a *Engine running* y repetir. |
| El login dice "Usuario o contraseña incorrectos" justo después de levantar | Todavía están arrancando los servicios. Esperar 2-3 minutos y mirar Eureka (tienen que figurar los 6). |
| Un servicio se reinicia las primeras veces | Es normal: espera al Config Server y a su base. Se estabiliza en 1-2 minutos. Ver: `docker compose logs -f <servicio>`. |
| Todo va muy lento o se cae | Son 9 servicios Java, 4 MySQL y RabbitMQ. Darle a Docker Desktop 6 GB o más de memoria (Settings, Resources). |
| Un puerto está ocupado | Cerrar lo que lo use (3307-3310, 4200, 5672, 8081, 8761, etc.) o frenar el contenedor viejo con `docker compose down`. |
