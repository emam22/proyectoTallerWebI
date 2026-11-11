package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EventoPartido;
import com.tallerwebi.dominio.RepositorioEventoPartido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioEventoPartidoImpl implements RepositorioEventoPartido {

  private final SessionFactory sf;

  @Autowired
  public RepositorioEventoPartidoImpl(SessionFactory sf) {
    this.sf = sf;
  }

  @Override
  public void guardar(EventoPartido evento) {
    sf.getCurrentSession().persist(evento);
  }

  @Override
  public List<EventoPartido> obtenerEventosPorPartido(Long idPartido) {
    return sf
      .getCurrentSession()
      .createQuery(
        "SELECT e FROM EventoPartido e WHERE e.partido.id = :idPartido ORDER BY e.minuto",
        EventoPartido.class
      )
      .setParameter("idPartido", idPartido)
      .getResultList();
  }
}
