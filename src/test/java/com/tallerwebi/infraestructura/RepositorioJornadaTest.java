package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.Jornada;
import com.tallerwebi.dominio.Torneo;
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
public class RepositorioJornadaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioJornadaImpl repoJornada;

  @BeforeEach
  void init() {
    repoJornada = new RepositorioJornadaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaJornada() {
    //given
    Torneo torneo = givenCreacionTorneo("Mega Futbol");

    sessionFactory.getCurrentSession().persist(torneo);

    Jornada jornada = new Jornada();
    jornada.setNumero(1);
    jornada.setTorneo(torneo);

    //when
    repoJornada.guardar(jornada);

    //then
    Jornada jornadaObtenida = sessionFactory
      .getCurrentSession()
      .createQuery("from Jornada where torneo.id = :idTorneo", Jornada.class)
      .setParameter("idTorneo", torneo.getId())
      .uniqueResult();
    assertThat(jornadaObtenida, is(notNullValue()));
    assertThat(jornadaObtenida.getNumero(), is(1));
  }

  private Torneo givenCreacionTorneo(String nombre) {
    Torneo torneo = new Torneo();
    torneo.setNombre(nombre);
    torneo.setCantidadDeEquipos(4);
    torneo.setIdaYVuelta(false);
    torneo.setCantidadMinJugadores(5);
    torneo.setCantidadMaxJugadores(10);
    torneo.setCantidadAmaSusp(3);

    return torneo;
  }

  @Transactional
  @Rollback
  @Test
  public void deberiaGuardarTodasLasJornadasDelTorneoDelFixture() {
    //given
    Torneo torneo = givenCreacionTorneo("Mega Futbol");
    sessionFactory.getCurrentSession().persist(torneo);

    Jornada j1 = new Jornada();
    j1.setNumero(1);
    torneo.agregarJornada(j1);

    Jornada j2 = new Jornada();
    j2.setNumero(2);
    torneo.agregarJornada(j2);

    List<Jornada> jornadas = new ArrayList<>();
    jornadas.add(j1);
    jornadas.add(j2);

    //when
    repoJornada.guardarListaFixture(jornadas);

    //then
    List<Jornada> jornadasObtenidas = repoJornada.obtenerJornadasPorTorneo(torneo.getId());

    assertThat(jornadasObtenidas.size(), is(torneo.getJornadas().size()));
  }

  @Transactional
  @Rollback
  @Test
  public void deberiaObtenerSolamenteLasJornadasDelTorneoSolicitado() {
    //given
    Torneo torneo1 = givenCreacionTorneo("Mega Futbol");
    Torneo torneo2 = givenCreacionTorneo("Futbol Nocturno");

    sessionFactory.getCurrentSession().persist(torneo1);
    sessionFactory.getCurrentSession().persist(torneo2);

    Jornada j1 = new Jornada();
    j1.setNumero(1);
    torneo1.agregarJornada(j1);

    Jornada j2 = new Jornada();
    j2.setNumero(2);
    torneo2.agregarJornada(j2);

    repoJornada.guardar(j1);
    repoJornada.guardar(j2);

    //when

    List<Jornada> jornadasObtenidas = repoJornada.obtenerJornadasPorTorneo(torneo1.getId());

    //then
    assertThat(jornadasObtenidas.get(0).getNumero(), is(1));
    assertThat(jornadasObtenidas.get(0).getTorneo().getId(), is(torneo1.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerLasJornadasOrdenadasPorNumero() {
    // given
    Torneo torneo = givenCreacionTorneo("Mega Futbol");
    sessionFactory.getCurrentSession().persist(torneo);

    Jornada j3 = new Jornada();
    j3.setNumero(3);
    torneo.agregarJornada(j3);

    Jornada j1 = new Jornada();
    j1.setNumero(1);
    torneo.agregarJornada(j1);

    Jornada j2 = new Jornada();
    j2.setNumero(2);
    torneo.agregarJornada(j2);

    repoJornada.guardar(j3);
    repoJornada.guardar(j1);
    repoJornada.guardar(j2);

    // When
    List<Jornada> jornadas = repoJornada.obtenerJornadasPorTorneo(torneo.getId());

    // Then
    assertThat(jornadas.get(0).getNumero(), is(1));
    assertThat(jornadas.get(1).getNumero(), is(2));
    assertThat(jornadas.get(2).getNumero(), is(3));
  }
}
