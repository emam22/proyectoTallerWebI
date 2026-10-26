package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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
public class RepositorioEstadisticasJugadorTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEstadisticaJugador repositorioEstadisticaJugador;

  @BeforeEach
  public void init() {
    repositorioEstadisticaJugador = new RepositorioEstadisticaJugadorImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnRegistroEstadisticaJugador() {
    // preparacion
    EstadisticaJugador estadisticaJugador = new EstadisticaJugador();

    // ejecucion
    repositorioEstadisticaJugador.guardar(estadisticaJugador);

    // verificacion
    EstadisticaJugador estadisticaObtenida = sessionFactory
      .getCurrentSession()
      .createQuery("from EstadisticaJugador ", EstadisticaJugador.class)
      .uniqueResult();

    assertThat(estadisticaObtenida, is(notNullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarListEstadisticas() {
    EstadisticaJugador est1 = new EstadisticaJugador();
    EstadisticaJugador est2 = new EstadisticaJugador();

    repositorioEstadisticaJugador.guardar(est1);
    repositorioEstadisticaJugador.guardar(est2);

    List<EstadisticaJugador> estadisticas = repositorioEstadisticaJugador.obtenerEstadisticas();

    assertNotNull(estadisticas);
    assertTrue(!estadisticas.isEmpty());
    assertEquals(2, estadisticas.size());
    assertTrue(estadisticas.contains(est1));
    assertTrue(estadisticas.contains(est2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBorrarRegistroEstadistica() {
    EstadisticaJugador est1 = new EstadisticaJugador();
    EstadisticaJugador est2 = new EstadisticaJugador();

    Jugador jugador = new Jugador();

    sessionFactory.getCurrentSession().save(jugador);

    est1.setJugador(jugador);

    repositorioEstadisticaJugador.guardar(est1);
    repositorioEstadisticaJugador.guardar(est2);

    List<EstadisticaJugador> estadisticas = repositorioEstadisticaJugador.obtenerEstadisticas();

    assertNotNull(estadisticas);
    assertTrue(!estadisticas.isEmpty());
    assertEquals(2, estadisticas.size());
    assertTrue(estadisticas.contains(est1));
    assertTrue(estadisticas.contains(est2));

    repositorioEstadisticaJugador.eliminarFilaEstadistica(est1.getJugador().getId());

    List<EstadisticaJugador> estadisticasActualizadas =
      repositorioEstadisticaJugador.obtenerEstadisticas();

    assertNotNull(estadisticasActualizadas);
    assertTrue(!estadisticasActualizadas.isEmpty());
    assertEquals(1, estadisticasActualizadas.size());
    assertTrue(!estadisticasActualizadas.contains(est1));
    assertTrue(estadisticasActualizadas.contains(est2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarEstadisticasPorIdJugador() {
    // Preparación
    Jugador jugador = new Jugador();
    sessionFactory.getCurrentSession().persist(jugador);

    EstadisticaJugador estadistica = new EstadisticaJugador();
    estadistica.setJugador(jugador);
    repositorioEstadisticaJugador.guardar(estadistica);

    // Ejecución
    EstadisticaJugador resultado = repositorioEstadisticaJugador.buscarEstadisticasPorIdJugador(
      jugador.getId()
    );

    // Verificación
    assertEquals(jugador.getId(), resultado.getJugador().getId());
    assertEquals(estadistica.getId(), resultado.getId());
  }
}
