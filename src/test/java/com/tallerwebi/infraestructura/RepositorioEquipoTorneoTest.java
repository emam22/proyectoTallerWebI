package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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
  public void deberiaBuscarLasFilasDelTorneoPedido() {
    // preparacion
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo Estadisticas");
    sessionFactory.getCurrentSession().persist(torneo);

    Equipo equipoA = new Equipo();
    equipoA.setNombre("Equipo Estadisticas A");
    sessionFactory.getCurrentSession().persist(equipoA);

    Equipo equipoB = new Equipo();
    equipoB.setNombre("Equipo Estadisticas B");
    sessionFactory.getCurrentSession().persist(equipoB);

    EquipoTorneo filaA = new EquipoTorneo();
    filaA.setEquipo(equipoA);
    filaA.setTorneo(torneo);
    filaA.setPuntos(9);
    filaA.setPartidosJugados(4);
    sessionFactory.getCurrentSession().persist(filaA);

    EquipoTorneo filaB = new EquipoTorneo();
    filaB.setEquipo(equipoB);
    filaB.setTorneo(torneo);
    filaB.setPuntos(3);
    filaB.setPartidosJugados(4);
    sessionFactory.getCurrentSession().persist(filaB);

    // ejecucion
    List<EquipoTorneo> filas = repositorioEquipoTorneo.buscarPorTorneo(torneo.getId());

    // validacion
    assertThat(filas, hasSize(2));
    assertThat(
      filas.stream().anyMatch(f -> "Equipo Estadisticas A".equals(f.getEquipo().getNombre())),
      is(true)
    );
    assertThat(filas.get(0).getTorneo().getId(), equalTo(torneo.getId()));
  }
}
