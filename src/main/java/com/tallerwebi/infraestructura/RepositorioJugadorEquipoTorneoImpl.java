package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.JugadorEquipoTorneo;
import com.tallerwebi.dominio.RepositorioJugadorEquipoTorneo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioJugadorEquipoTorneo")
public class RepositorioJugadorEquipoTorneoImpl implements RepositorioJugadorEquipoTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioJugadorEquipoTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(JugadorEquipoTorneo inscripcion) {
    sessionFactory.getCurrentSession().merge(inscripcion);
  }

  @Override
  public JugadorEquipoTorneo buscarPorJugadorYTorneo(Long idJugador, Long idTorneo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from JugadorEquipoTorneo jet where jet.jugador.id = :idJugador " +
        "and jet.torneo.id = :idTorneo",
        JugadorEquipoTorneo.class
      )
      .setParameter("idJugador", idJugador)
      .setParameter("idTorneo", idTorneo)
      .uniqueResult();
  }

  @Override
  public List<JugadorEquipoTorneo> listarPorEquipoYTorneo(Long idEquipo, Long idTorneo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from JugadorEquipoTorneo jet where jet.equipo.id = :idEquipo " +
        "and jet.torneo.id = :idTorneo",
        JugadorEquipoTorneo.class
      )
      .setParameter("idEquipo", idEquipo)
      .setParameter("idTorneo", idTorneo)
      .getResultList();
  }
}
