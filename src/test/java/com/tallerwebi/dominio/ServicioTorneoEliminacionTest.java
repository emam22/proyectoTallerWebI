package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import com.tallerwebi.infraestructura.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class ServicioTorneoEliminacionTest {

  @Autowired
  private SessionFactory sessionFactory;

  private ServicioTorneo servicioTorneo;

  @BeforeEach
  public void init() {
    RepositorioTorneo repositorioTorneo = new RepositorioTorneoImpl(sessionFactory);
    RepositorioJornada repositorioJornada = new RepositorioJornadaImpl(sessionFactory);
    RepositorioEquipoTorneo repositorioEquipoTorneo = new RepositorioEquipoTorneoImpl(
      sessionFactory
    );
    RepositorioEventoPartido repositorioEventoPartido = new RepositorioEventoPartidoImpl(
      sessionFactory
    );
    RepositorioEstadisticaJugador repositorioEstadisticaJugador =
      new RepositorioEstadisticaJugadorImpl(sessionFactory);
    RepositorioJugadorEquipoTorneo repositorioJugadorEquipoTorneo =
      new RepositorioJugadorEquipoTorneoImpl(sessionFactory);

    servicioTorneo =
      new ServicioTorneoImpl(
        repositorioTorneo,
        mock(RepositorioEquipoImpl.class),
        repositorioJornada,
        repositorioEquipoTorneo,
        mock(ServicioEquipoTorneo.class),
        repositorioEventoPartido,
        repositorioEstadisticaJugador,
        repositorioJugadorEquipoTorneo
      );
  }

  @Test
  @Transactional
  @Rollback
  public void eliminarTorneoDebeBorrarTodasSusRelacionesAntesQueAlTorneo() {
    Usuario usuario = new Usuario();
    usuario.setEmail("eliminar@unlam.edu.ar");
    usuario.setPassword("1234");
    usuario.setRol("ADMIN");
    sessionFactory.getCurrentSession().persist(usuario);

    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo a eliminar");
    torneo.setUsuario(usuario);
    sessionFactory.getCurrentSession().persist(torneo);
    Long idTorneo = torneo.getId();

    Equipo equipo = new Equipo();
    equipo.setNombre("Equipo a eliminar");
    sessionFactory.getCurrentSession().persist(equipo);

    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);
    equipoTorneo.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(equipoTorneo);

    Jugador jugador = new Jugador();
    jugador.setNombre("Jugador");
    jugador.setApellido("De prueba");
    sessionFactory.getCurrentSession().persist(jugador);

    JugadorEquipoTorneo inscripcion = new JugadorEquipoTorneo();
    inscripcion.setJugador(jugador);
    inscripcion.setEquipo(equipo);
    inscripcion.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(inscripcion);

    EstadisticaJugador estadistica = new EstadisticaJugador();
    estadistica.setJugador(jugador);
    estadistica.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(estadistica);

    Jornada jornada = new Jornada();
    jornada.setNumero(1);
    jornada.setTorneo(torneo);

    Partido partido = new Partido();
    partido.setEquipoLocal(equipo);
    partido.setEquipoVisitante(equipo);
    jornada.agregarPartido(partido);
    sessionFactory.getCurrentSession().persist(jornada);
    sessionFactory.getCurrentSession().flush();

    EventoPartido evento = new EventoPartido();
    evento.setTipo(TipoEventoPartido.GOL);
    evento.setMinuto(10);
    evento.setPartido(partido);
    evento.setJugador(jugador);
    sessionFactory.getCurrentSession().persist(evento);
    sessionFactory.getCurrentSession().flush();

    servicioTorneo.eliminarTorneo(idTorneo);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    assertThat(contar("select count(e) from EventoPartido e"), equalTo(0L));
    assertThat(contar("select count(p) from Partido p"), equalTo(0L));
    assertThat(contar("select count(j) from Jornada j"), equalTo(0L));
    assertThat(contar("select count(e) from EstadisticaJugador e"), equalTo(0L));
    assertThat(contar("select count(i) from JugadorEquipoTorneo i"), equalTo(0L));
    assertThat(contar("select count(e) from EquipoTorneo e"), equalTo(0L));
    assertThat(contar("select count(t) from Torneo t where t.id = :id", idTorneo), equalTo(0L));
    assertThat(contar("select count(j) from Jugador j"), equalTo(1L));
  }

  private Long contar(String hql, Object... parametros) {
    var query = sessionFactory.getCurrentSession().createQuery(hql, Long.class);

    if (parametros.length == 1) {
      query.setParameter("id", parametros[0]);
    }

    return query.uniqueResult();
  }
}
