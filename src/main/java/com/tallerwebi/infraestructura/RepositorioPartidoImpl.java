package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioPartido;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPartidoImpl implements RepositorioPartido {

  SessionFactory sf;

  @Autowired
  public RepositorioPartidoImpl(SessionFactory sf) {
    this.sf = sf;
  }

  @Override
  public void guardarPartido(Partido partido) {
    sf.getCurrentSession().persist(partido);
  }

  @Override
  public Partido buscarPorId(Long idPartido) {
    Partido partidoBuscado = sf
      .getCurrentSession()
      .createQuery("Select p from Partido p where id = :idPartido", Partido.class)
      .setParameter("idPartido", idPartido)
      .uniqueResult();
    return partidoBuscado;
  }
}
