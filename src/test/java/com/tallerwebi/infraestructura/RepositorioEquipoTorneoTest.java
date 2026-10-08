package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EquipoTorneo;
import com.tallerwebi.dominio.RepositorioEquipoTorneo;
import com.tallerwebi.dominio.Torneo;
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

}


