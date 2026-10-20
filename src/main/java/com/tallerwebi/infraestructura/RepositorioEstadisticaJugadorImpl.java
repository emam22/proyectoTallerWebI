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
}
