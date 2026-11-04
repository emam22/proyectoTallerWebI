package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioSimulacionTorneoImpl implements ServicioSimulacionTorneo {

  private final ServicioTorneo servicioTorneo;
  private final RepositorioJornada repositorioJornada;
  private final RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo;
  private final RepositorioEstadisticaJugador repositorioEstadisticaJugador;

  public ServicioSimulacionTorneoImpl(
    ServicioTorneo servicioTorneo,
    RepositorioJornada repositorioJornada,
    RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo,
    RepositorioEstadisticaJugador repositorioEstadisticaJugador
  ) {
    this.servicioTorneo = servicioTorneo;
    this.repositorioJornada = repositorioJornada;
    this.repositorioJugadorEquipoTorneo = repositorioJugadorEquipoTorneo;
    this.repositorioEstadisticaJugador = repositorioEstadisticaJugador;
  }

  // Busca la próxima jornada pendiente y simula sus partidos.
  @Override
  public Jornada avanzarJornada(Long idTorneo) {
    // Obtenemos todas las jornadas del torneo, ordenadas por número.
    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneo);

    Jornada jornadaPendiente = null;

    // Buscamos la primera jornada que todavía no fue finalizada.
    for (Jornada jornada : jornadas) {
      if (!"Finalizada".equalsIgnoreCase(jornada.getEstado())) {
        jornadaPendiente = jornada;
        break;
      }
    }

    // Si no quedan jornadas pendientes, no hay nada que simular.
    if (jornadaPendiente == null) {
      return null;
    }

    // Simulamos solamente los partidos que todavía no se disputaron.
    for (Partido partido : jornadaPendiente.getPartidos()) {
      if (!Boolean.TRUE.equals(partido.getDisputado())) {
        simularPartido(partido, idTorneo);
      }
    }

    // Verificamos si todos los partidos de la jornada fueron disputados.
    boolean todosDisputados = jornadaPendiente
      .getPartidos()
      .stream()
      .allMatch(partido -> Boolean.TRUE.equals(partido.getDisputado()));

    // Si todos terminaron, finalizamos la jornada.
    if (todosDisputados) {
      jornadaPendiente.setEstado("Finalizada");
    }

    // Guardamos los cambios realizados.
    repositorioJornada.actualizar(jornadaPendiente);

    return jornadaPendiente;
  }

  // Simula un partido entre dos equipos.
  private void simularPartido(Partido partido, Long idTorneo) {
    // Buscamos los jugadores inscriptos de cada equipo en este torneo.
    List<JugadorEquipoTorneo> jugadoresLocal =
      repositorioJugadorEquipoTorneo.listarPorEquipoYTorneo(
        partido.getEquipoLocal().getId(),
        idTorneo
      );

    List<JugadorEquipoTorneo> jugadoresVisitante =
      repositorioJugadorEquipoTorneo.listarPorEquipoYTorneo(
        partido.getEquipoVisitante().getId(),
        idTorneo
      );

    // Calculamos los porcentajes de ataque y defensa de ambos equipos.
    EstadisticasEquipo local = calcularEstadisticas(jugadoresLocal);
    EstadisticasEquipo visitante = calcularEstadisticas(jugadoresVisitante);

    // Calculamos la probabilidad de gol de cada equipo.
    double probabilidadLocal = calcularProbabilidadGol(
      local.getAtaque(),
      visitante.getDefensa(),
      true
    );

    double probabilidadVisitante = calcularProbabilidadGol(
      visitante.getAtaque(),
      local.getDefensa(),
      false
    );

    // Generamos los goles de cada equipo de manera aleatoria.
    int golesLocal = generarGoles(probabilidadLocal);
    int golesVisitante = generarGoles(probabilidadVisitante);

    // NUEVO: actualizamos las estadísticas individuales de ambos equipos.
    actualizarEstadisticasPartido(jugadoresLocal, jugadoresVisitante, golesLocal, golesVisitante);

    // Actualizamos el resultado y marcamos el partido como disputado.
    partido.setGolesLocal(golesLocal);
    partido.setGolesVisitante(golesVisitante);
    partido.setDisputado(true);
  }

  // Calcula los porcentajes de ataque y defensa de un equipo.
  private EstadisticasEquipo calcularEstadisticas(List<JugadorEquipoTorneo> inscripciones) {
    int puntosAtaque = 0;
    int puntosDefensa = 0;
    int totalJugadores = 0;

    for (JugadorEquipoTorneo inscripcion : inscripciones) {
      if (!esJugadorElegible(inscripcion)) {
        continue;
      }

      int[] puntos = obtenerPuntosPorPuesto(inscripcion.getJugador().getPuesto());

      puntosAtaque += puntos[0];
      puntosDefensa += puntos[1];
      totalJugadores++;
    }

    if (totalJugadores == 0) {
      throw new IllegalStateException(
        "Un equipo no tiene jugadores habilitados para disputar el partido."
      );
    }

    double puntosTotales = totalJugadores * 10.0;

    double ataque = (puntosAtaque / puntosTotales) * 100;
    double defensa = (puntosDefensa / puntosTotales) * 100;

    return new EstadisticasEquipo(ataque, defensa);
  }

  // Verifica si un jugador está habilitado y puede jugar esta jornada.
  private boolean esJugadorElegible(JugadorEquipoTorneo inscripcion) {
    if (inscripcion == null || inscripcion.getJugador() == null) {
      return false;
    }

    Jugador jugador = inscripcion.getJugador();

    // El jugador debe estar habilitado.
    if (!Boolean.TRUE.equals(jugador.getHabilitado())) {
      return false;
    }

    // El jugador no debe tener fechas de suspensión pendientes.
    Integer suspensiones = inscripcion.getFechasSuspensionPendientes();

    return suspensiones == null || suspensiones <= 0;
  }

  private int[] obtenerPuntosPorPuesto(String puesto) {
    if (puesto == null || puesto.isBlank()) {
      // Si no tiene puesto asignado, distribuimos los 10 puntos.
      return new int[] { 5, 5 };
    }

    switch (puesto.trim().toUpperCase(Locale.ROOT)) {
      case "DELANTERO":
        return new int[] { 10, 0 };
      case "MEDIOCAMPISTA":
      case "MEDIOCAMIPISTA":
        return new int[] { 6, 4 };
      case "DEFENSOR":
      case "ARQUERO":
        return new int[] { 0, 10 };
      default:
        // Puesto desconocido: distribución equilibrada.
        return new int[] { 5, 5 };
    }
  }

  // Calcula la probabilidad de que un equipo convierta un gol.
  private double calcularProbabilidadGol(double ataque, double defensaRival, boolean esLocal) {
    // Partimos de una probabilidad base del 30%.
    double probabilidad = 30 + ataque - defensaRival;

    // El equipo local recibe una ventaja de 3 puntos porcentuales.
    if (esLocal) {
      probabilidad += 3;
    }

    // Limitamos la probabilidad para evitar valores extremos.
    probabilidad = Math.max(10, Math.min(probabilidad, 55));

    // Convertimos el porcentaje a un valor entre 0 y 1.
    return probabilidad / 100.0;
  }

  // Genera los goles mediante cinco oportunidades aleatorias.
  private int generarGoles(double probabilidad) {
    int goles = 0;
    int oportunidades = 5;

    for (int i = 0; i < oportunidades; i++) {
      // Generamos un número aleatorio entre 0 (incluido) y 1 (excluido).
      double numeroAleatorio = ThreadLocalRandom.current().nextDouble();

      // Si el número es menor que la probabilidad, se convierte un gol.
      if (numeroAleatorio < probabilidad) {
        goles++;
      }
    }

    return goles;
  }

  // Clase auxiliar para guardar los porcentajes de cada equipo.
  private static class EstadisticasEquipo {

    private final double ataque;
    private final double defensa;

    public EstadisticasEquipo(double ataque, double defensa) {
      this.ataque = ataque;
      this.defensa = defensa;
    }

    public double getAtaque() {
      return ataque;
    }

    public double getDefensa() {
      return defensa;
    }
  }

  // Actualiza las estadísticas de los jugadores de ambos equipos.
  private void actualizarEstadisticasPartido(
    List<JugadorEquipoTorneo> jugadoresLocal,
    List<JugadorEquipoTorneo> jugadoresVisitante,
    int golesLocal,
    int golesVisitante
  ) {
    List<EstadisticaJugador> estadisticasLocal = obtenerEstadisticasJugadores(jugadoresLocal);

    List<EstadisticaJugador> estadisticasVisitante = obtenerEstadisticasJugadores(
      jugadoresVisitante
    );

    incrementarPartidosJugados(estadisticasLocal);
    incrementarPartidosJugados(estadisticasVisitante);

    asignarGolesYAsistencias(estadisticasLocal, golesLocal);
    asignarGolesYAsistencias(estadisticasVisitante, golesVisitante);

    asignarTarjetas(estadisticasLocal);
    asignarTarjetas(estadisticasVisitante);
  }

  // Recupera las estadísticas o crea un registro en cero.
  private List<EstadisticaJugador> obtenerEstadisticasJugadores(
    List<JugadorEquipoTorneo> inscripciones
  ) {
    List<EstadisticaJugador> estadisticas = new ArrayList<>();

    for (JugadorEquipoTorneo inscripcion : inscripciones) {
      if (!esJugadorElegible(inscripcion)) {
        continue;
      }

      Jugador jugador = inscripcion.getJugador();

      EstadisticaJugador estadistica = repositorioEstadisticaJugador.buscarEstadisticasPorIdJugador(
        jugador.getId()
      );

      if (estadistica == null) {
        estadistica = new EstadisticaJugador();
        estadistica.setJugador(jugador);
        estadistica.setPartidosJugados(0);
        estadistica.setGoles(0);
        estadistica.setAsistencias(0);
        estadistica.setParticipacionesGol(0);
        estadistica.setAmarillas(0);
        estadistica.setRojas(0);

        repositorioEstadisticaJugador.guardar(estadistica);
      }

      estadisticas.add(estadistica);
    }

    return estadisticas;
  }

  // Incrementa los partidos jugados.
  private void incrementarPartidosJugados(List<EstadisticaJugador> estadisticas) {
    for (EstadisticaJugador estadistica : estadisticas) {
      estadistica.setPartidosJugados(valorActual(estadistica.getPartidosJugados()) + 1);

      repositorioEstadisticaJugador.actualizarEstadisticasJugador(estadistica);
    }
  }

  // Asigna goles y asistencias aleatoriamente.
  private void asignarGolesYAsistencias(List<EstadisticaJugador> estadisticas, int cantidadGoles) {
    if (estadisticas.isEmpty()) {
      return;
    }

    for (int i = 0; i < cantidadGoles; i++) {
      int indiceGoleador = ThreadLocalRandom.current().nextInt(estadisticas.size());

      EstadisticaJugador goleador = estadisticas.get(indiceGoleador);

      goleador.setGoles(valorActual(goleador.getGoles()) + 1);

      goleador.setParticipacionesGol(valorActual(goleador.getParticipacionesGol()) + 1);

      repositorioEstadisticaJugador.actualizarEstadisticasJugador(goleador);

      boolean tieneAsistencia =
        estadisticas.size() > 1 && ThreadLocalRandom.current().nextDouble() < 0.70;

      if (tieneAsistencia) {
        int indiceAsistente;

        do {
          indiceAsistente = ThreadLocalRandom.current().nextInt(estadisticas.size());
        } while (indiceAsistente == indiceGoleador);

        EstadisticaJugador asistente = estadisticas.get(indiceAsistente);

        asistente.setAsistencias(valorActual(asistente.getAsistencias()) + 1);

        asistente.setParticipacionesGol(valorActual(asistente.getParticipacionesGol()) + 1);

        repositorioEstadisticaJugador.actualizarEstadisticasJugador(asistente);
      }
    }
  }

  // Asigna tarjetas aleatorias.
  private void asignarTarjetas(List<EstadisticaJugador> estadisticas) {
    if (estadisticas.isEmpty()) {
      return;
    }

    int cantidadAmarillas = ThreadLocalRandom.current().nextInt(3);

    for (int i = 0; i < cantidadAmarillas; i++) {
      EstadisticaJugador jugador = elegirJugador(estadisticas);

      jugador.setAmarillas(valorActual(jugador.getAmarillas()) + 1);

      repositorioEstadisticaJugador.actualizarEstadisticasJugador(jugador);
    }

    boolean recibeRoja = ThreadLocalRandom.current().nextInt(100) < 8;

    if (recibeRoja) {
      EstadisticaJugador jugador = elegirJugador(estadisticas);

      jugador.setRojas(valorActual(jugador.getRojas()) + 1);

      repositorioEstadisticaJugador.actualizarEstadisticasJugador(jugador);
    }
  }

  // Selecciona un jugador aleatorio.
  private EstadisticaJugador elegirJugador(List<EstadisticaJugador> estadisticas) {
    int indice = ThreadLocalRandom.current().nextInt(estadisticas.size());

    return estadisticas.get(indice);
  }

  // Evita errores cuando una estadística es null.
  private int valorActual(Integer valor) {
    return valor == null ? 0 : valor;
  }
}
