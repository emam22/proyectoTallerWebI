package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ResumenControllerTest {

  private ServicioTorneo servicioTorneoMock;
  private RepositorioEstadisticaJugador repositorioEstadisticasMock;
  private RepositorioJugadorEquipoTorneo repositorioInscripcionesMock;
  private RepositorioEventoPartido repositorioEventosMock;

  private ResumenController controlador;

  @BeforeEach
  public void init() {
    servicioTorneoMock = mock(ServicioTorneo.class);
    repositorioEstadisticasMock = mock(RepositorioEstadisticaJugador.class);
    repositorioInscripcionesMock = mock(RepositorioJugadorEquipoTorneo.class);
    repositorioEventosMock = mock(RepositorioEventoPartido.class);

    controlador =
      new ResumenController(
        servicioTorneoMock,
        repositorioEstadisticasMock,
        repositorioInscripcionesMock,
        repositorioEventosMock
      );
  }

  @Test
  public void cargarTorneoDebeConsultarElServicioSoloConId() {
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    assertSame(torneo, controlador.cargarTorneo(1L));
    assertNull(controlador.cargarTorneo(null));
    verify(servicioTorneoMock, times(1)).consultarTorneoPorId(1L);
  }

  @Test
  public void irAResumenSinTorneoOSinJornadaDebeMostrarError() {
    ModelAndView sinTorneo = controlador.irAResumen(dto(null, 5L));
    assertThat(sinTorneo.getViewName(), equalTo("resumen"));
    assertThat(
      sinTorneo.getModel().get("error"),
      equalTo("No se encontró el torneo o la jornada solicitada.")
    );

    Torneo torneo = new Torneo();
    torneo.setId(1L);
    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);

    ModelAndView sinJornada = controlador.irAResumen(dto(1L, null));
    assertThat(sinJornada.getViewName(), equalTo("resumen"));
    assertThat(
      sinJornada.getModel().get("error"),
      equalTo("No se encontró el torneo o la jornada solicitada.")
    );
  }

  @Test
  public void irAResumenConJornadaInexistenteDebeMostrarError() {
    Torneo torneo = new Torneo();
    torneo.setId(1L);
    Jornada jornada = new Jornada();
    jornada.setId(5L);

    when(servicioTorneoMock.consultarTorneoPorId(1L)).thenReturn(torneo);
    when(servicioTorneoMock.obtenerJornadasPorTorneo(1L)).thenReturn(List.of(jornada));

    ModelAndView mav = controlador.irAResumen(dto(1L, 99L));

    assertThat(mav.getViewName(), equalTo("resumen"));
    assertThat(mav.getModel().get("error"), equalTo("No se encontró la jornada solicitada."));
  }

  @Test
  public void irAResumenDebeExponerEstadisticasYEventosDelPartido() {
    Long idTorneo = 1L;
    Long idJornada = 5L;

    Torneo torneo = new Torneo();
    torneo.setId(idTorneo);

    Equipo local = equipo(10L, "Boca");
    Equipo visitante = equipo(20L, "River");

    Partido partido = new Partido();
    partido.setId(7L);
    partido.setEquipoLocal(local);
    partido.setEquipoVisitante(visitante);

    Jornada jornada = new Jornada();
    jornada.setId(idJornada);
    jornada.getPartidos().add(partido);

    Jugador goleador = jugador(100L, "Lionel", "Messi");
    Jugador asistidor = jugador(101L, "Angel", "Di Maria");
    Jugador otro = jugador(300L, "Sin", "Inscripcion");

    JugadorEquipoTorneo inscripcionLocal = inscripcion(goleador, local);
    JugadorEquipoTorneo inscripcionAsistidor = inscripcion(asistidor, local);
    JugadorEquipoTorneo inscripcionVisitante = inscripcion(
      jugador(200L, "Juan", "Perez"),
      visitante
    );

    when(servicioTorneoMock.consultarTorneoPorId(idTorneo)).thenReturn(torneo);
    when(servicioTorneoMock.obtenerJornadasPorTorneo(idTorneo)).thenReturn(List.of(jornada));
    when(repositorioEstadisticasMock.obtenerEstadisticas())
      .thenReturn(
        Arrays.asList(
          estadistica(goleador),
          estadistica(asistidor),
          estadistica(inscripcionVisitante.getJugador())
        )
      );
    when(repositorioInscripcionesMock.listarPorEquipoYTorneo(10L, idTorneo))
      .thenReturn(Arrays.asList(inscripcionLocal, inscripcionAsistidor));
    when(repositorioInscripcionesMock.listarPorEquipoYTorneo(20L, idTorneo))
      .thenReturn(List.of(inscripcionVisitante));
    when(repositorioInscripcionesMock.buscarPorJugadorYTorneo(100L, idTorneo))
      .thenReturn(inscripcionLocal);
    when(repositorioInscripcionesMock.buscarPorJugadorYTorneo(101L, idTorneo))
      .thenReturn(inscripcionAsistidor);
    when(repositorioEventosMock.obtenerEventosPorPartido(7L))
      .thenReturn(
        Arrays.asList(
          evento(TipoEventoPartido.GOL, 23, goleador, asistidor),
          evento(TipoEventoPartido.AMARILLA, 45, null, null),
          evento(TipoEventoPartido.ROJA, 80, otro, null)
        )
      );

    ModelAndView mav = controlador.irAResumen(dto(idTorneo, idJornada));

    assertThat(mav.getViewName(), equalTo("resumen"));
    assertThat(mav.getModel().get("idTorneoActual"), equalTo(idTorneo));
    assertThat(mav.getModel().get("jornada"), equalTo(jornada));
    assertThat((List<Partido>) mav.getModel().get("partidos"), hasSize(1));

    @SuppressWarnings("unchecked")
    List<ResumenPartidoDTO> resumenPartidos = (List<ResumenPartidoDTO>) mav
      .getModel()
      .get("resumenPartidos");
    assertThat(resumenPartidos, hasSize(1));

    ResumenPartidoDTO resumen = resumenPartidos.get(0);
    assertThat(resumen.getPartido(), equalTo(partido));
    assertThat(resumen.getEstadisticasLocal(), hasSize(2));
    assertThat(resumen.getEstadisticasVisitante(), hasSize(1));
    assertThat(resumen.getEventos(), hasSize(3));

    EventoPartidoDto gol = resumen.getEventos().get(0);
    assertThat(gol.getTipo(), equalTo(TipoEventoPartido.GOL));
    assertThat(gol.getMinuto(), equalTo(23));
    assertThat(gol.getNombreJugador(), equalTo("Lionel Messi"));
    assertThat(gol.getNombreAsistidor(), equalTo("Angel Di Maria"));
    assertThat(gol.getNombreEquipo(), equalTo("Boca"));

    assertThat(resumen.getEventos().get(1).getNombreJugador(), nullValue());
    assertThat(resumen.getEventos().get(2).getNombreEquipo(), nullValue());
    assertThat(mav.getModel().get("error"), nullValue());
    assertThat(repositorioEventosMock, notNullValue());
  }

  private BuscarResumenDTO dto(Long idTorneo, Long idJornada) {
    BuscarResumenDTO dto = new BuscarResumenDTO();
    dto.setIdTorneo(idTorneo);
    dto.setIdJornada(idJornada);
    return dto;
  }

  private Equipo equipo(Long id, String nombre) {
    Equipo equipo = new Equipo();
    equipo.setId(id);
    equipo.setNombre(nombre);
    return equipo;
  }

  private Jugador jugador(Long id, String nombre, String apellido) {
    Jugador jugador = new Jugador();
    jugador.setId(id);
    jugador.setNombre(nombre);
    jugador.setApellido(apellido);
    return jugador;
  }

  private JugadorEquipoTorneo inscripcion(Jugador jugador, Equipo equipo) {
    JugadorEquipoTorneo inscripcion = new JugadorEquipoTorneo();
    inscripcion.setJugador(jugador);
    inscripcion.setEquipo(equipo);
    return inscripcion;
  }

  private EstadisticaJugador estadistica(Jugador jugador) {
    EstadisticaJugador estadistica = new EstadisticaJugador();
    estadistica.setJugador(jugador);
    return estadistica;
  }

  private EventoPartido evento(
    TipoEventoPartido tipo,
    Integer minuto,
    Jugador jugador,
    Jugador asistidor
  ) {
    EventoPartido evento = new EventoPartido();
    evento.setTipo(tipo);
    evento.setMinuto(minuto);
    evento.setJugador(jugador);
    evento.setAsistidor(asistidor);
    return evento;
  }
}
