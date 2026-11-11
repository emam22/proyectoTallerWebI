package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EstadisticaJugador;
import com.tallerwebi.dominio.Jugador;
import com.tallerwebi.dominio.RepositorioEstadisticaJugador;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("RepositorioEquipojugador")
public class RepositorioEstadisticaJugadorImpl implements RepositorioEstadisticaJugador {

  SessionFactory sf;

  @Autowired
  public RepositorioEstadisticaJugadorImpl(SessionFactory sf) {
    this.sf = sf;
  }

  @Override
  public void guardar(EstadisticaJugador estadisticaJugador) {
    sf.getCurrentSession().persist(estadisticaJugador);
  }

  @Override
  public List<EstadisticaJugador> obtenerEstadisticas() {
    return sf.getCurrentSession().createQuery("Select e from EstadisticaJugador e").getResultList();
  }

  @Override
  public void eliminarFilaEstadistica(Long idJugador) {
    sf
      .getCurrentSession()
      .createQuery("Delete From EstadisticaJugador e Where e.jugador.id = :idJugador")
      .setParameter("idJugador", idJugador)
      .executeUpdate();
  }

  @Override
  public EstadisticaJugador buscarEstadisticasPorIdJugador(Long idJugador) {
    return sf
      .getCurrentSession()
      .createQuery(
        "SELECT e FROM EstadisticaJugador e WHERE e.jugador.id = :idJugador",
        EstadisticaJugador.class
      )
      .setParameter("idJugador", idJugador)
      .uniqueResult();
  }

  @Override
  public void actualizarEstadisticasJugador(EstadisticaJugador estadisticas) {
    sf.getCurrentSession().merge(estadisticas);
  }

  @Override
  public List<EstadisticaJugador> obtenerEstadisticasPorTorneo(Long idTorneo) {
    return sf
      .getCurrentSession()
      .createQuery(
        "SELECT e FROM EstadisticaJugador e WHERE e.torneo.id = :idTorneo ORDER BY e.goles DESC, e.asistencias DESC",
        EstadisticaJugador.class
      )
      .setParameter("idTorneo", idTorneo)
      .getResultList();
  }

  @Override
  public EstadisticaJugador buscarEstadisticasPorJugadorYTorneo(Long idJugador, Long idTorneo) {
    List<EstadisticaJugador> resultados = sf
      .getCurrentSession()
      .createQuery(
        "SELECT e FROM EstadisticaJugador e WHERE e.jugador.id = :idJugador AND e.torneo.id = :idTorneo",
        EstadisticaJugador.class
      )
      .setParameter("idJugador", idJugador)
      .setParameter("idTorneo", idTorneo)
      .getResultList();

    return resultados.isEmpty() ? null : resultados.get(0);
  }
}
