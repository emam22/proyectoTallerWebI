package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Fecha;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioFixture;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioFixture")
public class RepositorioFixtureImpl implements RepositorioFixture {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioFixtureImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Fecha> buscarFechasPorTorneo(Long idTorneo) {
    List<Fecha> fechas = sessionFactory
      .getCurrentSession()
      .createQuery("from Fecha where torneo.id = :idTorneo order by numero", Fecha.class)
      .setParameter("idTorneo", idTorneo)
      .getResultList();
    for (Fecha fecha : fechas) {
      // Partido guarda la FK como fechaId copiado, así que se carga por consulta aparte
      fecha.setPartidos(buscarPartidosDeLaFecha(fecha.getId()));
    }
    return fechas;
  }

  @Override
  public void guardarFecha(Fecha fecha) {
    sessionFactory.getCurrentSession().persist(fecha);
  }

  @Override
  public void guardarPartido(Partido partido) {
    sessionFactory.getCurrentSession().persist(partido);
  }

  private List<Partido> buscarPartidosDeLaFecha(Long idFecha) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Partido where fechaId = :idFecha order by id", Partido.class)
      .setParameter("idFecha", idFecha)
      .getResultList();
  }
}
