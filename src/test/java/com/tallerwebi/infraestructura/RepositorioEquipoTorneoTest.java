package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    // Preparación
    Long idTorneo = 999999L;

    // Ejecución
    int cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(et) " + "from EquipoTorneo et " + "where et.torneo.id = :idTorneo",
        Long.class
      )
      .setParameter("idTorneo", idTorneo)
      .uniqueResult()
      .intValue();

    // Verificación
    assertThat(cantidad, is(equalTo(0)));
  }
}
