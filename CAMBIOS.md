# Cambios

## Fixes
- **NPE en el login**: un usuario registrado no tenia `rol`, y el login hacía `getRol().equalsIgnoreCase(...)` → error 500. Ahora es seguro (no lanza) y al registrarse se asigna `rol = "usuario"` por defecto.
- **data.sql**: corregido el nombre de la tabla de `usuario` a `Usuario` (en Linux los nombres son case-sensitive, por eso fallaba el arranque).

## Tests
- Tests de las vistas nuevas del pull de Juan Pablo (`lobbyAdmin`, `formulario-torneo`, `creacion-equipo`, `administrarTorneo`).
- Test de login de usuario con `rol` null.
- Test de que `registrar()` asigna el rol por defecto.

## Otros
- Cache de templates Thymeleaf desactivada (solo para desarrollo: los cambios en HTML se ven sin reiniciar).
- Commiteados como 4 commits y pusheados a `origin/main`.

## Para correr
```bash
DB_PORT=3307 mvn clean jetty:run
```

## Usuarios de prueba
- `admin@gmail.com` / `1234` → `/lobbyAdmin`
- `usuario@gmail.com` / `1234` → `/lobby`
- `test@unlam.edu.ar` / `test` → ADMIN