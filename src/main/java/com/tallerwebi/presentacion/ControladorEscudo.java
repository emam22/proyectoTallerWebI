package com.tallerwebi.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControladorEscudo {

  @ResponseBody
  @PostMapping(value = "/subirEscudo", consumes = "application/octet-stream")
  public ResponseEntity<String> subirEscudo(
    @RequestBody byte[] contenido,
    @RequestParam("nombre") String nombreOriginal,
    HttpServletRequest request
  ) {
    ResponseEntity<String> validacion = validarImagen(contenido, nombreOriginal);

    if (validacion != null) {
      return validacion;
    }

    String extension = obtenerExtension(nombreOriginal);

    try {
      String rutaReal = request.getServletContext().getRealPath("/resources/core/img/escudos");

      if (rutaReal == null) {
        return ResponseEntity
          .internalServerError()
          .body("No se encontró el directorio para guardar los escudos.");
      }

      Path directorio = Paths.get(rutaReal);
      Files.createDirectories(directorio);

      String nombreArchivo = UUID.randomUUID() + extension;
      Path destino = directorio.resolve(nombreArchivo);

      Files.write(destino, contenido);

      return ResponseEntity.ok("/img/escudos/" + nombreArchivo);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body("No se pudo guardar el escudo.");
    }
  }

  private String obtenerExtension(String nombreOriginal) {
    String nombre = nombreOriginal.toLowerCase(Locale.ROOT);

    if (nombre.endsWith(".png")) {
      return ".png";
    }
    if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
      return ".jpg";
    }
    if (nombre.endsWith(".webp")) {
      return ".webp";
    }

    return "";
  }

  private String obtenerFormatoEsperado(String extension) {
    return switch (extension) {
      case ".png" -> "png";
      case ".jpg" -> "jpeg";
      case ".webp" -> "webp";
      default -> "";
    };
  }

  private String obtenerFormatoImagen(byte[] contenido) {
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

    return "";
  }

  private boolean coincideFirma(byte[] contenido, byte[] firma, int inicio) {
    if (contenido.length < inicio + firma.length) {
      return false;
    }

    return Arrays.equals(Arrays.copyOfRange(contenido, inicio, inicio + firma.length), firma);
  }

  private ResponseEntity<String> validarImagen(byte[] contenido, String nombreOriginal) {
    if (contenido == null || contenido.length == 0) {
      return ResponseEntity.badRequest().body("La imagen está vacía.");
    }

    if (contenido.length > 10 * 1024 * 1024) {
      return ResponseEntity.badRequest().body("La imagen no puede superar los 10 MB.");
    }

    if (nombreOriginal == null || nombreOriginal.isBlank()) {
      return ResponseEntity.badRequest().body("No se recibió el nombre de la imagen.");
    }

    String extension = obtenerExtension(nombreOriginal);

    if (extension.isEmpty()) {
      return ResponseEntity.badRequest().body("Solo se permiten imágenes PNG, JPG o WEBP.");
    }

    String formato = obtenerFormatoImagen(contenido);

    if (!formato.equals(obtenerFormatoEsperado(extension))) {
      return ResponseEntity.badRequest().body("El contenido no coincide con el formato de imagen.");
    }

    return null;
  }
}
