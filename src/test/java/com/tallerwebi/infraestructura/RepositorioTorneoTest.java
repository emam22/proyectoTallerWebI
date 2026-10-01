package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.RepositorioTorneo;
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
public class RepositorioTorneoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTorneo repositorioTorneo;

  @BeforeEach
  public void init() {
    repositorioTorneo = new RepositorioTorneoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnNuevoTorneo() {
    String nombreTorneo = "MegaFutbol";
    //preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre(nombreTorneo);
    //ejecucion
    repositorioTorneo.guardar(torneo);
    //validacion
    Torneo torneoObtenido = sessionFactory
            .getCurrentSession()
            .createQuery("from Torneo where nombre = :nombre", Torneo.class)
            .setParameter("nombre", nombreTorneo)
            .uniqueResult();

    assertThat(torneoObtenido.getNombre(), is(equalTo(nombreTorneo)));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarUnTorneoInexistenteCuandoBuscoPorNombre() {
    //preparacion
    String nombreTorneo = "Campito";

    //ejecucion
    Torneo torneoBuscado = repositorioTorneo.buscarTorneo(nombreTorneo);

    //validacion
    assertThat(torneoBuscado, is(nullValue()));
  }
}
