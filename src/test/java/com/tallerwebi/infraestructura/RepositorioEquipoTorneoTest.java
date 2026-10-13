package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.RepositorioEquipoTorneo;
import com.tallerwebi.dominio.Torneo;
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
public class RepositorioEquipoTorneoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEquipoTorneo repositorioEquipoTorneo;

  @BeforeEach
  public void init() {
    repositorioEquipoTorneo = new RepositorioEquipoTorneoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnRegistroEquipoTorneo() {
    // preparacion
    EquipoTorneo equipoTorneo = new EquipoTorneo();

    // ejecucion
    repositorioEquipoTorneo.guardar(equipoTorneo);

    // verificacion
    EquipoTorneo equipoTorneoObtenido = sessionFactory
      .getCurrentSession()
      .createQuery("from EquipoTorneo", EquipoTorneo.class)
      .uniqueResult();

    assertThat(equipoTorneoObtenido, is(notNullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarLosEquiposDeUnTorneo() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("MegaFutbol");

    sessionFactory.getCurrentSession().persist(torneo);

    EquipoTorneo equipoTorneo1 = new EquipoTorneo();
    equipoTorneo1.setTorneo(torneo);

    EquipoTorneo equipoTorneo2 = new EquipoTorneo();
    equipoTorneo2.setTorneo(torneo);

    sessionFactory.getCurrentSession().persist(equipoTorneo1);

    sessionFactory.getCurrentSession().persist(equipoTorneo2);

    Long idTorneo = torneo.getId();

    // ejecucion
    int cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(et) " + "from EquipoTorneo et " + "where et.torneo.id = :idTorneo",
        Long.class
      )
      .setParameter("idTorneo", idTorneo)
      .uniqueResult()
      .intValue();

    // verificacion
    assertThat(cantidad, is(equalTo(2)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarCeroSiElTorneoNoTieneEquipos() {
    // preparacion
    Long idTorneo = 999999L;

    // ejecucion
    int cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(et) " + "from EquipoTorneo et " + "where et.torneo.id = :idTorneo",
        Long.class
      )
      .setParameter("idTorneo", idTorneo)
      .uniqueResult()
      .intValue();

    // verificacion
    assertThat(cantidad, is(equalTo(0)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarTrueSiElEquipoEstaInscriptoEnElTorneo() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("MegaFutbol");

    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pumas");

    sessionFactory.getCurrentSession().persist(torneo);
    sessionFactory.getCurrentSession().persist(equipo);

    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);
    equipoTorneo.setTorneo(torneo);

    sessionFactory.getCurrentSession().persist(equipoTorneo);

    Long idEquipo = equipo.getId();
    Long idTorneo = torneo.getId();

    // ejecucion
    String hql =
      """
      SELECT COUNT(et)
      FROM EquipoTorneo et
      WHERE et.equipo.id = :idEquipo
      AND et.torneo.id = :idTorneo
      """;

    Long cantidad = (Long) this.sessionFactory.getCurrentSession()
      .createQuery(hql)
      .setParameter("idEquipo", idEquipo)
      .setParameter("idTorneo", idTorneo)
      .uniqueResult();

    boolean existe = cantidad > 0;

    // verificacion
    assertThat(existe, is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarFalseSiElEquipoEstaEnOtroTorneo() {
    // preparación
    Torneo torneo1 = new Torneo();
    torneo1.setNombre("MegaFutbol");

    Torneo torneo2 = new Torneo();
    torneo2.setNombre("SuperLiga");

    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pumas");

    sessionFactory.getCurrentSession().persist(torneo1);
    sessionFactory.getCurrentSession().persist(torneo2);
    sessionFactory.getCurrentSession().persist(equipo);

    EquipoTorneo equipoTorneo = new EquipoTorneo();
    equipoTorneo.setEquipo(equipo);
    equipoTorneo.setTorneo(torneo1);

    sessionFactory.getCurrentSession().persist(equipoTorneo);

    Long idEquipo = equipo.getId();
    Long idTorneo2 = torneo2.getId();

    // ejecucion
    String hql =
      """
      SELECT COUNT(et)
      FROM EquipoTorneo et
      WHERE et.equipo.id = :idEquipo
      AND et.torneo.id = :idTorneo
      """;

    Long cantidad = (Long) this.sessionFactory.getCurrentSession()
      .createQuery(hql)
      .setParameter("idEquipo", idEquipo)
      .setParameter("idTorneo", idTorneo2)
      .uniqueResult();

    boolean existe = cantidad > 0;

    // verificacion
    assertThat(existe, is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void obtenerTablaPosicionesDeberiaOrdenarPorPuntosYDiferenciaDeGoles() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo de prueba");

    sessionFactory.getCurrentSession().persist(torneo);

    Equipo equipo1 = new Equipo();
    equipo1.setNombre("Equipo A");

    Equipo equipo2 = new Equipo();
    equipo2.setNombre("Equipo B");

    Equipo equipo3 = new Equipo();
    equipo3.setNombre("Equipo C");

    sessionFactory.getCurrentSession().persist(equipo1);
    sessionFactory.getCurrentSession().persist(equipo2);
    sessionFactory.getCurrentSession().persist(equipo3);

    EquipoTorneo et1 = new EquipoTorneo();
    et1.setEquipo(equipo1);
    et1.setTorneo(torneo);
    et1.setPuntos(6);
    et1.setGolesAFavor(5);
    et1.setGolesEnContra(1);

    EquipoTorneo et2 = new EquipoTorneo();
    et2.setEquipo(equipo2);
    et2.setTorneo(torneo);
    et2.setPuntos(9);
    et2.setGolesAFavor(4);
    et2.setGolesEnContra(2);

    EquipoTorneo et3 = new EquipoTorneo();
    et3.setEquipo(equipo3);
    et3.setTorneo(torneo);
    et3.setPuntos(6);
    et3.setGolesAFavor(8);
    et3.setGolesEnContra(2);

    sessionFactory.getCurrentSession().persist(et1);
    sessionFactory.getCurrentSession().persist(et2);
    sessionFactory.getCurrentSession().persist(et3);

    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Long idTorneo = torneo.getId();

    // ejecucion
    List<EquipoTorneo> resultado = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select et from EquipoTorneo et " +
        "where et.torneo.id = :idTorneo " +
        "order by et.puntos desc, " +
        "(et.golesAFavor - et.golesEnContra) desc, " +
        "et.golesAFavor DESC",
        EquipoTorneo.class
      )
      .setParameter("idTorneo", idTorneo)
      .getResultList();

    // verificacion
    assertThat(resultado.size(), is(3));

    assertThat(resultado.get(0).getEquipo().getNombre(), equalTo("Equipo B"));

    assertThat(resultado.get(1).getEquipo().getNombre(), equalTo("Equipo C"));

    assertThat(resultado.get(2).getEquipo().getNombre(), equalTo("Equipo A"));
  }
}
