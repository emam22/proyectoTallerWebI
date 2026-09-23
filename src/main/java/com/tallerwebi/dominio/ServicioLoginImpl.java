package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) {
    return repositorioUsuario.buscarUsuario(email, password);
  }

  @Override
  @SuppressWarnings("PMD.CyclomaticComplexity") // Ignora la complejidad en este método
  public void registrar(Usuario usuario) throws UsuarioExistente, Exception {
    Usuario usuarioEncontrado = repositorioUsuario.buscarUsuario(
      usuario.getEmail(),
      usuario.getPassword()
    );

    if (usuarioEncontrado != null) {
      throw new UsuarioExistente();
    }
    String fortaleza = validarFortaleza(usuario.getPassword());

    switch (fortaleza) {
      case "FUERTE":
      case "MEDIANA":
        if (usuario.getRol() == null) {
          usuario.setRol("usuario");
        }
        repositorioUsuario.guardar(usuario);
        break;
      case "DEBIL":
        throw new Exception("La contraseña es demasiado debil");
      case "INVALIDA":
        throw new Exception("La contraseña no puede estar vacía.");
      default:
        throw new Exception("Error desconocido al validar la contraseña.");
    }
  }

  @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.AvoidLiteralsInIfCondition" }) // Ignora complejidad y literales aquí
  private String validarFortaleza(String password) {
    if (password == null || password.trim().isEmpty()) {
      return "INVALIDA";
    }
    int letras = 0;
    int numeros = 0;
    int especiales = 0;
    String caracteresPermitidos = "-_%$?!.,@";

    for (char c : password.toCharArray()) {
      if (Character.isLetter(c)) {
        letras++;
      } else if (Character.isDigit(c)) {
        numeros++;
      } else if (caracteresPermitidos.indexOf(c) >= 0) {
        especiales++;
      }
    }
    int longitud = password.length();

    if (longitud >= 8) {
      if (letras >= 4 && numeros >= 2 && especiales >= 2) {
        return "FUERTE";
      }
      if (numeros >= 1 && especiales >= 1) {
        return "MEDIANA";
      }
    }
    return "DEBIL";
  }
}
