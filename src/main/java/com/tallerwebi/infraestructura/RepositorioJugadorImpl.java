package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Jugador;
import com.tallerwebi.dominio.RepositorioJugador;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioJugador")
public class RepositorioJugadorImpl implements RepositorioJugador {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioJugadorImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Jugador jugador) {
    sessionFactory.getCurrentSession().persist(jugador);
  }

  @Override
  public void eliminar(Long idJugador) {
    Jugador jugador = sessionFactory.getCurrentSession().get(Jugador.class, idJugador);
    if (jugador != null) {
      sessionFactory.getCurrentSession().remove(jugador);
    }
  }

  @Override
  public List<Jugador> listarPorEquipo(Long idEquipo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select j from Equipo e join e.jugadores j where e.id = :id order by j.dorsal",
        Jugador.class
      )
      .setParameter("id", idEquipo)
      .getResultList();
  }
}
