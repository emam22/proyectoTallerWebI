package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.Jugador;
import com.tallerwebi.dominio.RepositorioJugador;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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
public class RepositorioJugadorTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioJugador repositorioJugador;

  @BeforeEach
  public void init() {
    this.repositorioJugador = new RepositorioJugadorImpl(this.sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnJugador() {
    // preparacion
    Jugador jugador = new Jugador();
    jugador.setNombre("Lionel");

    // ejecucion
    this.repositorioJugador.guardar(jugador);

    // verificacion
    Jugador obtenido =
      this.sessionFactory.getCurrentSession()
        .createQuery("from Jugador where nombre = :nombre", Jugador.class)
        .setParameter("nombre", "Lionel")
        .uniqueResult();

    assertThat(obtenido.getNombre(), is("Lionel"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaListarLosJugadoresDeUnEquipo() {
    // preparacion
    Jugador jugador = new Jugador();
    jugador.setDorsal(10);
    this.sessionFactory.getCurrentSession().persist(jugador);

    Equipo equipo = new Equipo();
    equipo.setJugadores(new ArrayList<>());
    equipo.getJugadores().add(jugador);
    this.sessionFactory.getCurrentSession().persist(equipo);

    // ejecucion
    List<Jugador> jugadores = this.repositorioJugador.listarPorEquipo(equipo.getId());

    // verificacion
    assertThat(jugadores, hasSize(1));
    assertThat(jugadores.get(0).getDorsal(), is(10));
  }

  @Test
  @Transactional
  public void deberiaDevolverListaVaciaCuandoElEquipoNoTieneJugadores() {
    // ejecucion
    List<Jugador> jugadores = this.repositorioJugador.listarPorEquipo(123456L);

    // verificacion
    assertThat(jugadores, hasSize(0));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEliminarUnJugador() {
    // preparacion
    Jugador jugador = new Jugador();
    jugador.setNombre("Borrar");
    this.sessionFactory.getCurrentSession().persist(jugador);
    Long id = jugador.getId();

    // ejecucion
    this.repositorioJugador.eliminar(id);

    // verificacion
    Jugador obtenido = this.sessionFactory.getCurrentSession().get(Jugador.class, id);
    assertThat(obtenido, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaActualizarElEstadoDeHabilitacionDeUnJugador() {
    // preparacion
    Jugador jugador = new Jugador();
    jugador.setNombre("Actualizable");
    this.sessionFactory.getCurrentSession().persist(jugador);

    // ejecucion
    jugador.setHabilitado(true);
    this.repositorioJugador.actualizar(jugador);
    this.sessionFactory.getCurrentSession().flush();

    // verificacion
    Jugador obtenido = this.sessionFactory.getCurrentSession().get(Jugador.class, jugador.getId());
    assertThat(obtenido.getHabilitado(), is(true));
  }
}
