package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
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
public class RepositorioJugadorEquipoTorneoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioJugadorEquipoTorneo repositorio;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioJugadorEquipoTorneoImpl(sessionFactory);
  }

  private JugadorEquipoTorneo crearInscripcion() {
    // Crear usuario necesario para el torneo
    Usuario usuario = new Usuario();
    sessionFactory.getCurrentSession().persist(usuario);

    // Crear entidades relacionadas
    Jugador jugador = new Jugador();
    sessionFactory.getCurrentSession().persist(jugador);

    Equipo equipo = new Equipo();
    equipo.setNombre("Boca");
    sessionFactory.getCurrentSession().persist(equipo);

    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo de prueba");
    torneo.setUsuario(usuario);
    sessionFactory.getCurrentSession().persist(torneo);

    JugadorEquipoTorneo inscripcion = new JugadorEquipoTorneo();
    inscripcion.setJugador(jugador);
    inscripcion.setEquipo(equipo);
    inscripcion.setTorneo(torneo);

    return inscripcion;
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaInscripcion() {
    // Preparación
    JugadorEquipoTorneo inscripcion = crearInscripcion();

    // Ejecución
    repositorio.guardar(inscripcion);

    // Verificación
    List<JugadorEquipoTorneo> inscripciones = sessionFactory
      .getCurrentSession()
      .createQuery("from JugadorEquipoTorneo", JugadorEquipoTorneo.class)
      .getResultList();

    assertThat(inscripciones, hasSize(1));
    assertThat(
      inscripciones.get(0).getJugador().getId(),
      equalTo(inscripcion.getJugador().getId())
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarInscripcionPorJugadorYTorneo() {
    // Preparación
    JugadorEquipoTorneo inscripcion = crearInscripcion();
    repositorio.guardar(inscripcion);

    // Ejecución
    JugadorEquipoTorneo resultado = repositorio.buscarPorJugadorYTorneo(
      inscripcion.getJugador().getId(),
      inscripcion.getTorneo().getId()
    );

    // Verificación
    assertThat(resultado, notNullValue());
    assertThat(resultado.getEquipo().getId(), equalTo(inscripcion.getEquipo().getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaListarInscripcionesPorEquipoYTorneo() {
    // Preparación
    JugadorEquipoTorneo inscripcion = crearInscripcion();
    repositorio.guardar(inscripcion);

    // Ejecución
    List<JugadorEquipoTorneo> resultado = repositorio.listarPorEquipoYTorneo(
      inscripcion.getEquipo().getId(),
      inscripcion.getTorneo().getId()
    );

    // Verificación
    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getJugador().getId(), equalTo(inscripcion.getJugador().getId()));
  }
}
