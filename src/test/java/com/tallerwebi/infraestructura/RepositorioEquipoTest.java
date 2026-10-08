package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.RepositorioEquipo;
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
public class RepositorioEquipoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEquipo repositorioEquipo;

  @BeforeEach
  public void init() {
    repositorioEquipo = new RepositorioEquipoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnNuevoEquipo() {
    // preparacion
    String nombreEquipo = "Boca";
    Equipo equipo = new Equipo();
    equipo.setNombre(nombreEquipo);

    // ejecucion
    repositorioEquipo.guardar(equipo);

    // verificacion
    Equipo equipoObtenido = sessionFactory
      .getCurrentSession()
      .createQuery("from Equipo where nombre = :nombre", Equipo.class)
      .setParameter("nombre", nombreEquipo)
      .uniqueResult();

    assertThat(equipoObtenido.getNombre(), is(equalTo(nombreEquipo)));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarUnEquipoInexistenteCuandoBuscoPorNombre() {
    //preparacion
    String nombreEquipo = "Boca";

    //ejecucion
    Equipo equipoBuscado = repositorioEquipo.buscarEquipo(nombreEquipo);

    //validacion
    assertThat(equipoBuscado, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnEquipoPorId() {
    //preparacion
    Equipo equipo = new Equipo();

    //ejecucion
    repositorioEquipo.guardar(equipo);

    Long id = equipo.getId();

    //validacion
    Equipo equipoObtenido = sessionFactory
      .getCurrentSession()
      .createQuery("from Equipo where id = :id", Equipo.class)
      .setParameter("id", id)
      .uniqueResult();

    assertThat(equipoObtenido.getId(), is(equalTo(id)));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarEquipoPorIdSiNoExisteDeberiaDevolverNull() {
    // preparacion
    Long id = 999999L;

    // ejecucion
    Equipo equipoObtenido = sessionFactory
      .getCurrentSession()
      .createQuery("from Equipo where id = :id", Equipo.class)
      .setParameter("id", id)
      .uniqueResult();

    // validacion
    assertThat(equipoObtenido, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaListarLosEquiposGuardados() {
    // preparacion
    Equipo equipo1 = new Equipo();
    equipo1.setNombre("Boca");

    Equipo equipo2 = new Equipo();
    equipo2.setNombre("River");

    sessionFactory.getCurrentSession().persist(equipo1);

    sessionFactory.getCurrentSession().persist(equipo2);

    // ejecucion
    List<Equipo> equipos = repositorioEquipo.listar();

    // verificacion
    assertThat(equipos, hasSize(2));
    assertThat(equipos, hasItems(equipo1, equipo2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRetornarListaVaciaSiNoHayEquipos() {
    // ejecucion
    List<Equipo> equipos = repositorioEquipo.listar();

    // verificacion
    assertThat(equipos, is(empty()));
  }
}
