# Entidad `JugadorEquipoTorneo`

## Problema que resuelve

Antes, la sanción de un jugador estaba guardada **en el jugador mismo**
(`Jugador.estadoSancion`, `Jugador.fechasSuspensionPendientes`).

El problema: un `Jugador` no pertenece a un torneo, pertenece a un **club**
(`Equipo`). Y ese mismo jugador puede jugar en varios torneos con ese mismo club.
Entonces, si el jugador se sancionaba en el torneo A, quedaba sancionado también
en el torneo B, porque la sanción era del jugador, no del torneo.

## Solución

Se creó una entidad intermedia que relaciona **jugador + equipo + torneo**, y la
sanción se movió ahí:

```
JugadorEquipoTorneo
├── jugador  (datos personales: nombre, apellido, etc.)
├── equipo   (club)
├── torneo   (competición)
├── estadoSancion
└── fechasSuspensionPendientes
```

Restricción única `(jugador, equipo, torneo)`: un jugador no puede estar
inscripto dos veces en el mismo torneo con el mismo club.

## Qué cambió en el código

1. **Al agregar un jugador** a un equipo en un torneo, se crea la relación
   `JugadorEquipoTorneo` con estado `SIN_SANCION`.

2. **Las sanciones ahora son por torneo:**
   - `sancionarJugadorEnTorneo(idJugador, idTorneo, fechas)`
   - `agregarFechasSuspensionEnTorneo(idJugador, idTorneo, fechas)`
   - `cumplirFechaSuspensionEnTorneo(idJugador, idTorneo)`

   Operan sobre la relación, no sobre el `Jugador`.

3. **Tarjetas amarillas/rojas** ahora sancionan en el torneo del partido
   (antes sancionaban globalmente).

4. **La vista de sanciones** (`jugadoresEquipoSancion`) muestra las fechas
   pendientes desde la relación, no desde el jugador.

## Resultado

Un jugador sancionado en el torneo A figura `SANCIONADO` solo ahí. En el torneo B,
con el mismo club, sigue `SIN_SANCION` y puede jugar sin problema.

## Compatibilidad

Los campos de sanción en `Jugador` se mantienen por ahora (no se rompieron vistas
ni tests existentes). La limpieza final de esos campos queda para después, una
vez que todo el flujo use la nueva entidad.
