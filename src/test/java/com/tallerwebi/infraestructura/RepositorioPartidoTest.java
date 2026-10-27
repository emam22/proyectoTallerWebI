package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class RepositorioPartidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPartido repositorioPartido;

  @BeforeEach
  public void init() {
    repositorioPartido = new RepositorioPartidoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnRegistroPartido() {
    // preparacion
    Partido partido = new Partido();

    // ejecucion
    repositorioPartido.guardarPartido(partido);

    // verificacion
    Partido partidoObtenido = sessionFactory
      .getCurrentSession()
      .createQuery("from Partido ", Partido.class)
      .uniqueResult();

    assertThat(partidoObtenido, is(notNullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnPartidoPorIdYObtenerlo() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("Clausura");
    sessionFactory.getCurrentSession().persist(torneo);

    Equipo equipoLocal = new Equipo();
    equipoLocal.setNombre("local fc");
    sessionFactory.getCurrentSession().persist(equipoLocal);

    Equipo equipoVisitante = new Equipo();
    equipoVisitante.setNombre("visitante fc");
    sessionFactory.getCurrentSession().persist(equipoVisitante);

    Jornada jornada = new Jornada();
    jornada.setNumero(1);
    jornada.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(jornada);

    Partido partido = new Partido();
    partido.setEquipoLocal(equipoLocal);
    partido.setEquipoVisitante(equipoVisitante);
    partido.setJornada(jornada);

    // ejecucion
    repositorioPartido.guardarPartido(partido);

    // verificacion
    Partido partidoBuscado = repositorioPartido.buscarPorId(partido.getId());

    assertThat(partidoBuscado, is(notNullValue()));
    assertThat(partidoBuscado.getId(), equalTo(partido.getId()));
  }
}
