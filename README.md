# Escuela TP - Cómo levantarlo

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| root | root123 | Root |
| director | director123 | Director |
| preceptor | preceptor123 | Preceptor |
| docente | docente123 | Docente |

## Opción 1: con Docker (recomendada)

Solo hace falta tener Docker instalado. No hay que compilar nada ni crear un `.env`.

1. Clonar el repo y entrar a la carpeta del compose:

```
   git clone https://github.com/maximiliano-occhiuzzi/escuelatp
   cd escuelatp/escuelatp
```

2. Levantar el sistema:

```
   docker compose -f docker-compose.hub.yml up -d
```

3. Docker lee el archivo, ve `image: maxmegafuego/escuelatp-gateway:latest` (y las demás imágenes `maxmegafuego/escuelatp-*`) y las baja automáticamente desde Docker Hub. Los servicios traen valores por defecto para las claves, por eso no se necesita `.env`.
4. Esperar 2-3 minutos a que todos los servicios se registren, y abrir:

   | Qué | URL |
   |---|---|
   | Front | http://localhost:4200 |
   | Eureka (servicios registrados) | http://localhost:8761 |
   | RabbitMQ | http://localhost:15672 (escuela / escuela) |
   | MySQL | localhost:3307 (root / escuela) |

Para apagarlo: `docker compose -f docker-compose.hub.yml down` (agregar `-v` para borrar también los datos).

## Opción 2: con Docker, desde el código fuente

Construye las imágenes en la propia máquina. Acá sí hace falta un `.env`, porque no se sube a Git (está en el `.gitignore`).
El front vive en otro repo: Docker lo clona y lo construye solo desde https://github.com/maximiliano-occhiuzzi/escuelatp-front (requiere internet).

1. Copiar `.env.example` como `.env` en la carpeta `escuelatp` y completar los valores.
2. Correr:

```
   docker compose up --build
```

## Opción 3: sin Docker (Eclipse / IDE)

Agregar la variable de entorno `JWT_SECRET` en **Auth** y en **Gateway**. Tiene que ser **el mismo valor** en los dos, si no, el token no valida y no funciona el login.

## Generar el JWT_SECRET en Windows

`openssl` no existe en Windows. En PowerShell:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```
