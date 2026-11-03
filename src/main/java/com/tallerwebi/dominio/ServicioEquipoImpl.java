package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EquipoExistente;
import com.tallerwebi.dominio.excepcion.SiglaExistente;
import com.tallerwebi.dominio.excepcion.SiglaInvalida;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioEquipo")
@Transactional
public class ServicioEquipoImpl implements ServicioEquipo {

  private RepositorioEquipo repositorioEquipo;

  @Autowired
  public ServicioEquipoImpl(RepositorioEquipo repositorioEquipo) {
    this.repositorioEquipo = repositorioEquipo;
  }

  @Override
  public Equipo consultarEquipoPorId(Long id) {
    return repositorioEquipo.buscarPorId(id);
  }

  @Override
  public Equipo consultarEquipo(String nombre) {
    return repositorioEquipo.buscarEquipo(nombre);
  }

  @Override
  public void registrarEquipo(Equipo equipo) throws EquipoExistente, SiglaExistente, SiglaInvalida {
    Equipo equipoEncontrado = repositorioEquipo.buscarEquipo(equipo.getNombre());

    if (equipoEncontrado != null) {
      throw new EquipoExistente();
    }

    Equipo equipoConSigla = repositorioEquipo.buscarEquipoPorSigla(equipo.getSigla());

    if (equipoConSigla != null) {
      throw new SiglaExistente();
    }

    validarSigla(equipo.getSigla());

    repositorioEquipo.guardar(equipo);
  }

  @Override
  public List<Equipo> obtenerEquipos() {
    return repositorioEquipo.listar();
  }

  private void validarSigla(String sigla) throws SiglaInvalida {
    if (sigla == null || sigla.trim().length() < 3 || sigla.trim().length() > 5) {
      throw new SiglaInvalida();
    }
  }

  @Override
  public void actualizarEquipo(Equipo equipo)
    throws EquipoExistente, SiglaExistente, SiglaInvalida {
    Equipo equipoActual = repositorioEquipo.buscarPorId(equipo.getId());

    Equipo equipoConMismoNombre = repositorioEquipo.buscarEquipo(equipo.getNombre());

    if (equipoConMismoNombre != null && !equipoConMismoNombre.getId().equals(equipo.getId())) {
      throw new EquipoExistente();
    }

    Equipo equipoConMismaSigla = repositorioEquipo.buscarEquipoPorSigla(equipo.getSigla());

    if (equipoConMismaSigla != null && !equipoConMismaSigla.getId().equals(equipo.getId())) {
      throw new SiglaExistente();
    }

    if (equipo.getEscudo() != null && !equipo.getEscudo().isBlank()) {
      equipoActual.setEscudo(equipo.getEscudo());
    }

    validarSigla(equipo.getSigla());

    repositorioEquipo.actualizar(equipo);
  }

  @Override
  public Equipo consultarEquipoJugador(Long idJugador) {
    return repositorioEquipo.buscarEquipoPorIdJugador(idJugador);
  }

  //metodos de subir escudo
  @Override
  public String subirEscudo(byte[] contenido, String nombreOriginal, String rutaReal)
    throws IOException {
    validarImagen(contenido, nombreOriginal);

    String extension = validarExtension(nombreOriginal);

    Path directorio = Paths.get(rutaReal);
    Files.createDirectories(directorio);

    String nombreArchivo = UUID.randomUUID() + extension;
    Path destino = directorio.resolve(nombreArchivo);

    Files.write(destino, contenido);

    return "/img/escudos/" + nombreArchivo;
  }

  private void validarImagen(byte[] contenido, String nombreOriginal) {
    validarContenido(contenido);
    validarNombre(nombreOriginal);

    String extension = validarExtension(nombreOriginal);

    if (extension.isEmpty()) {
      throw new IllegalArgumentException("Solo se permiten imágenes PNG, JPG, WEBP o SVG.");
    }

    String formato = validarFormatoImagen(contenido);

    if (!esFormatoValido(extension, formato)) {
      throw new IllegalArgumentException("El contenido no coincide con el formato de imagen.");
    }
  }

  private String validarExtension(String nombreOriginal) {
    String nombre = nombreOriginal.toLowerCase(Locale.ROOT);

    if (nombre.endsWith(".png")) return ".png";
    if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) return ".jpg";
    if (nombre.endsWith(".webp")) return ".webp";
    if (nombre.endsWith(".svg")) return ".svg";

    return "";
  }

  private String validarFormatoImagen(byte[] contenido) {
    if (coincideFirma(contenido, new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 }, 0)) {
      return "png";
    }

    if (coincideFirma(contenido, new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }, 0)) {
      return "jpeg";
    }

    if (
      coincideFirma(contenido, new byte[] { 'R', 'I', 'F', 'F' }, 0) &&
      coincideFirma(contenido, new byte[] { 'W', 'E', 'B', 'P' }, 8)
    ) {
      return "webp";
    }

    if (esSvg(contenido)) {
      return "svg";
    }

    return "";
  }

  private boolean esSvg(byte[] contenido) {
    String texto = new String(contenido, StandardCharsets.UTF_8).trim().toLowerCase(Locale.ROOT);

    return texto.contains("<svg") && texto.contains("</svg>");
  }

  private boolean coincideFirma(byte[] contenido, byte[] firma, int inicio) {
    if (contenido.length < inicio + firma.length) {
      return false;
    }

    return Arrays.equals(Arrays.copyOfRange(contenido, inicio, inicio + firma.length), firma);
  }

  private void validarContenido(byte[] contenido) {
    if (contenido == null || contenido.length == 0) {
      throw new IllegalArgumentException("La imagen está vacía.");
    }

    if (contenido.length > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("La imagen no puede superar los 10 MB.");
    }
  }

  private void validarNombre(String nombreOriginal) {
    if (nombreOriginal == null || nombreOriginal.isBlank()) {
      throw new IllegalArgumentException("No se recibió el nombre de la imagen.");
    }
  }

  private boolean esFormatoValido(String extension, String formato) {
    return (
      (".png".equals(extension) && "png".equals(formato)) ||
      (".jpg".equals(extension) && "jpeg".equals(formato)) ||
      (".webp".equals(extension) && "webp".equals(formato)) ||
      (".svg".equals(extension) && "svg".equals(formato))
    );
  }
}
