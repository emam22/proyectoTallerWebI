package com.tallerwebi.dominio;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioSimulacionTorneoImpl implements ServicioSimulacionTorneo {

  private final ServicioTorneo servicioTorneo;
  private final ServicioEquipoTorneo servicioEquipoTorneo;
  private final RepositorioJornada repositorioJornada;
  private final RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo;
  private final RepositorioEstadisticaJugador repositorioEstadisticaJugador;
  private final RepositorioEventoPartido repositorioEventoPartido;
  private final ServicioJugador servicioJugador;

  public ServicioSimulacionTorneoImpl(
    ServicioTorneo servicioTorneo,
    ServicioEquipoTorneo servicioEquipoTorneo,
    RepositorioJornada repositorioJornada,
    RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo,
    RepositorioEstadisticaJugador repositorioEstadisticaJugador,
    RepositorioEventoPartido repositorioEventoPartido,
    ServicioJugador servicioJugador
  ) {
    this.servicioTorneo = servicioTorneo;
    this.servicioEquipoTorneo = servicioEquipoTorneo;
    this.repositorioJornada = repositorioJornada;
    this.repositorioJugadorEquipoTorneo = repositorioJugadorEquipoTorneo;
    this.repositorioEstadisticaJugador = repositorioEstadisticaJugador;
    this.repositorioEventoPartido = repositorioEventoPartido;
    this.servicioJugador = servicioJugador;
  }

  @Override
  public Jornada avanzarJornada(Long idTorneo) {
    List<Jornada> jornadas = servicioTorneo.obtenerJornadasPorTorneo(idTorneo);
    Jornada jornadaPendiente = null;

    for (Jornada jornada : jornadas) {
      if (!"Finalizada".equalsIgnoreCase(jornada.getEstado())) {
        jornadaPendiente = jornada;
        break;
      }
    }

    if (jornadaPendiente == null) {
      return null;
    }

    Torneo torneoActual = servicioTorneo.consultarTorneoPorId(idTorneo);
    Set<Long> jugadoresSuspendidos = obtenerSuspendidosDeLaJornada(jornadaPendiente, idTorneo);

    for (Partido partido : jornadaPendiente.getPartidos()) {
      if (!Boolean.TRUE.equals(partido.getDisputado())) {
        simularPartido(partido, torneoActual);
      }
    }

    boolean todosDisputados = jornadaPendiente
      .getPartidos()
      .stream()
      .allMatch(partido -> Boolean.TRUE.equals(partido.getDisputado()));

    if (todosDisputados) {
      jornadaPendiente.setEstado("Finalizada");
      cumplirSuspensionesDeLaJornada(jugadoresSuspendidos, idTorneo);
    }

    repositorioJornada.actualizar(jornadaPendiente);
    return jornadaPendiente;
  }

  private void simularPartido(Partido partido, Torneo torneo) {
    Long idTorneo = torneo.getId();

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

    EstadisticasEquipo local = calcularEstadisticas(jugadoresLocal);
    EstadisticasEquipo visitante = calcularEstadisticas(jugadoresVisitante);

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

    int golesLocal = generarGoles(probabilidadLocal);
    int golesVisitante = generarGoles(probabilidadVisitante);

    actualizarEstadisticasPartido(
      partido,
      jugadoresLocal,
      jugadoresVisitante,
      golesLocal,
      golesVisitante,
      torneo
    );

    // IMPACTO EN LA TABLA DE POSICIONES DEL EQUIPO EN EL TORNEO
    servicioEquipoTorneo.actualizarTablaPosiciones(
      idTorneo,
      partido.getEquipoLocal().getId(),
      partido.getEquipoVisitante().getId(),
      golesLocal,
      golesVisitante
    );

    partido.setGolesLocal(golesLocal);
    partido.setGolesVisitante(golesVisitante);
    partido.setDisputado(true);
  }

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

  private boolean esJugadorElegible(JugadorEquipoTorneo inscripcion) {
    if (inscripcion == null || inscripcion.getJugador() == null) {
      return false;
    }

    Jugador jugador = inscripcion.getJugador();

    if (!Boolean.TRUE.equals(jugador.getHabilitado())) {
      return false;
    }

    Integer suspensiones = inscripcion.getFechasSuspensionPendientes();
    return suspensiones == null || suspensiones <= 0;
  }

  private int[] obtenerPuntosPorPuesto(String puesto) {
    if (puesto == null || puesto.isBlank()) {
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
        return new int[] { 5, 5 };
    }
  }

  private double calcularProbabilidadGol(double ataque, double defensaRival, boolean esLocal) {
    double probabilidad = 30 + ataque - defensaRival;

    if (esLocal) {
      probabilidad += 3;
    }

    probabilidad = Math.max(10, Math.min(probabilidad, 55));
    return probabilidad / 100.0;
  }

  private int generarGoles(double probabilidad) {
    int goles = 0;
    int oportunidades = 5;

    for (int i = 0; i < oportunidades; i++) {
      double numeroAleatorio = ThreadLocalRandom.current().nextDouble();
      if (numeroAleatorio < probabilidad) {
        goles++;
      }
    }

    return goles;
  }

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

  private void actualizarEstadisticasPartido(
    Partido partido,
    List<JugadorEquipoTorneo> jugadoresLocal,
    List<JugadorEquipoTorneo> jugadoresVisitante,
    int golesLocal,
    int golesVisitante,
    Torneo torneo
  ) {
    List<EstadisticaJugador> estadisticasLocal = obtenerEstadisticasJugadores(
      jugadoresLocal,
      torneo
    );
    List<EstadisticaJugador> estadisticasVisitante = obtenerEstadisticasJugadores(
      jugadoresVisitante,
      torneo
    );

    incrementarPartidosJugados(estadisticasLocal);
    incrementarPartidosJugados(estadisticasVisitante);

    asignarGolesYAsistencias(partido, estadisticasLocal, golesLocal);
    asignarGolesYAsistencias(partido, estadisticasVisitante, golesVisitante);

    asignarTarjetas(partido, estadisticasLocal, torneo.getId());
    asignarTarjetas(partido, estadisticasVisitante, torneo.getId());
  }

  private List<EstadisticaJugador> obtenerEstadisticasJugadores(
    List<JugadorEquipoTorneo> inscripciones,
    Torneo torneo
  ) {
    List<EstadisticaJugador> estadisticas = new ArrayList<>();

    for (JugadorEquipoTorneo inscripcion : inscripciones) {
      if (!esJugadorElegible(inscripcion)) {
        continue;
      }

      Jugador jugador = inscripcion.getJugador();

      EstadisticaJugador estadistica =
        repositorioEstadisticaJugador.buscarEstadisticasPorJugadorYTorneo(
          jugador.getId(),
          torneo.getId()
        );

      if (estadistica == null) {
        estadistica = new EstadisticaJugador();
        estadistica.setJugador(jugador);
        estadistica.setTorneo(torneo);
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

  private void incrementarPartidosJugados(List<EstadisticaJugador> estadisticas) {
    for (EstadisticaJugador estadistica : estadisticas) {
      estadistica.setPartidosJugados(valorActual(estadistica.getPartidosJugados()) + 1);
      repositorioEstadisticaJugador.actualizarEstadisticasJugador(estadistica);
    }
  }

  private void asignarGolesYAsistencias(
    Partido partido,
    List<EstadisticaJugador> estadisticas,
    int cantidadGoles
  ) {
    if (estadisticas.isEmpty()) {
      return;
    }

    for (int i = 0; i < cantidadGoles; i++) {
      int indiceGoleador = ThreadLocalRandom.current().nextInt(estadisticas.size());
      EstadisticaJugador goleador = estadisticas.get(indiceGoleador);

      goleador.setGoles(valorActual(goleador.getGoles()) + 1);
      goleador.setParticipacionesGol(valorActual(goleador.getParticipacionesGol()) + 1);
      repositorioEstadisticaJugador.actualizarEstadisticasJugador(goleador);

      Jugador asistidor = null;
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

        asistidor = asistente.getJugador();
      }

      registrarEvento(partido, TipoEventoPartido.GOL, goleador.getJugador(), asistidor);
    }
  }

  private void registrarEvento(
    Partido partido,
    TipoEventoPartido tipo,
    Jugador jugador,
    Jugador asistidor
  ) {
    EventoPartido evento = new EventoPartido();
    evento.setTipo(tipo);
    evento.setMinuto(ThreadLocalRandom.current().nextInt(1, 91));
    evento.setPartido(partido);
    evento.setJugador(jugador);
    evento.setAsistidor(asistidor);

    repositorioEventoPartido.guardar(evento);
  }

  private void asignarTarjetas(
    Partido partido,
    List<EstadisticaJugador> estadisticas,
    Long idTorneo
  ) {
    if (estadisticas.isEmpty()) {
      return;
    }

    List<EstadisticaJugador> jugadoresConAmarilla = new ArrayList<>();
    int cantidadAmarillas = ThreadLocalRandom.current().nextInt(3);

    for (int i = 0; i < cantidadAmarillas; i++) {
      EstadisticaJugador jugador = elegirJugador(estadisticas);

      if (jugadoresConAmarilla.contains(jugador)) {
        continue;
      }

      jugador.setAmarillas(valorActual(jugador.getAmarillas()) + 1);
      repositorioEstadisticaJugador.actualizarEstadisticasJugador(jugador);

      registrarEvento(partido, TipoEventoPartido.AMARILLA, jugador.getJugador(), null);
      jugadoresConAmarilla.add(jugador);
    }

    boolean recibeRoja = ThreadLocalRandom.current().nextInt(100) < 8;

    if (recibeRoja) {
      EstadisticaJugador jugador = elegirJugador(estadisticas);

      jugador.setRojas(valorActual(jugador.getRojas()) + 1);
      repositorioEstadisticaJugador.actualizarEstadisticasJugador(jugador);

      registrarEvento(partido, TipoEventoPartido.ROJA, jugador.getJugador(), null);
      servicioJugador.sancionarJugadorEnTorneo(jugador.getJugador().getId(), idTorneo, 1);
    }
  }

  private EstadisticaJugador elegirJugador(List<EstadisticaJugador> estadisticas) {
    int indice = ThreadLocalRandom.current().nextInt(estadisticas.size());
    return estadisticas.get(indice);
  }

  private int valorActual(Integer valor) {
    return valor == null ? 0 : valor;
  }

  private Set<Long> obtenerSuspendidosDeLaJornada(Jornada jornada, Long idTorneo) {
    Set<Long> jugadoresSuspendidos = new HashSet<>();

    for (Partido partido : jornada.getPartidos()) {
      if (!Boolean.TRUE.equals(partido.getDisputado())) {
        agregarSuspendidosDelEquipo(
          partido.getEquipoLocal().getId(),
          idTorneo,
          jugadoresSuspendidos
        );
        agregarSuspendidosDelEquipo(
          partido.getEquipoVisitante().getId(),
          idTorneo,
          jugadoresSuspendidos
        );
      }
    }

    return jugadoresSuspendidos;
  }

  private void agregarSuspendidosDelEquipo(
    Long idEquipo,
    Long idTorneo,
    Set<Long> jugadoresSuspendidos
  ) {
    List<JugadorEquipoTorneo> inscripciones = repositorioJugadorEquipoTorneo.listarPorEquipoYTorneo(
      idEquipo,
      idTorneo
    );

    for (JugadorEquipoTorneo inscripcion : inscripciones) {
      if (inscripcion == null || inscripcion.getJugador() == null) {
        continue;
      }

      Integer pendientes = inscripcion.getFechasSuspensionPendientes();
      if (pendientes != null && pendientes > 0 && inscripcion.getJugador().getId() != null) {
        jugadoresSuspendidos.add(inscripcion.getJugador().getId());
      }
    }
  }

  private void cumplirSuspensionesDeLaJornada(Set<Long> jugadoresSuspendidos, Long idTorneo) {
    for (Long idJugador : jugadoresSuspendidos) {
      servicioJugador.cumplirFechaSuspensionEnTorneo(idJugador, idTorneo);
    }
  }
}
