package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControladorEscudoTest {

  private ControladorEscudo controlador;
  private HttpServletRequest request;
  private ServletContext servletContext;

  @TempDir
  Path directorioTemporal;

  @BeforeEach
  public void init() {
    controlador = new ControladorEscudo();
    request = mock(HttpServletRequest.class);
    servletContext = mock(ServletContext.class);

    when(request.getServletContext()).thenReturn(servletContext);
  }

  @Test
  public void deberiaRechazarUnaImagenVacia() {
    byte[] img = new byte[0];

    ResponseEntity<String> respuesta = controlador.subirEscudo(
      img,
      "escudo.png",
      request // se guarda la img vacia
    );

    assertThat(respuesta.getStatusCode(), is(HttpStatus.BAD_REQUEST)); // la img esta vacia
    assertThat(respuesta.getBody(), is("La imagen está vacía."));
  }

  @Test
  public void deberiaRechazarUnaImagenNula() {
    ResponseEntity<String> respuesta = controlador.subirEscudo(
      null,
      "escudo.png",
      request //se setea la img null
    );

    assertThat(respuesta.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    assertThat(respuesta.getBody(), is("La imagen está vacía."));
  }

  @Test
  public void deberiaRechazarUnNombreVacio() {
    ResponseEntity<String> respuesta = controlador.subirEscudo(new byte[] { 1 }, " ", request);

    assertThat(respuesta.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    assertThat(respuesta.getBody(), is("No se recibió el nombre de la imagen."));
  }

  @Test
  public void deberiaRechazarUnaExtensionNoPermitida() {
    ResponseEntity<String> respuesta = controlador.subirEscudo(
      new byte[] { 1 },
      "escudo.gif",
      request // se rechaza la extension gif
    );

    assertThat(respuesta.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    assertThat(respuesta.getBody(), is("Solo se permiten imágenes PNG, JPG o WEBP."));
  }

  @Test
  public void deberiaRechazarUnContenidoQueNoCoincideConLaExtension() {
    ResponseEntity<String> respuesta = controlador.subirEscudo(
      new byte[] { 1, 2, 3 },
      "escudo.png",
      request
    );

    assertThat(respuesta.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    assertThat(respuesta.getBody(), is("El contenido no coincide con el formato de imagen."));
  }

  @Test
  public void deberiaGuardarUnaImagenPngValida() throws IOException {
    when(servletContext.getRealPath("/resources/core/img/escudos"))
      .thenReturn(directorioTemporal.toString());

    byte[] contenido = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x01, 0x02 };

    ResponseEntity<String> respuesta = controlador.subirEscudo(contenido, "escudo.png", request);

    assertThat(respuesta.getStatusCode(), is(HttpStatus.OK));
    assertThat(respuesta.getBody().startsWith("/img/escudos/"), is(true));
    assertThat(respuesta.getBody().endsWith(".png"), is(true));
  }

  @Test
  public void deberiaInformarSiNoEncuentraElDirectorio() {
    when(servletContext.getRealPath("/resources/core/img/escudos")).thenReturn(null);

    byte[] contenido = { (byte) 0x89, 0x50, 0x4E, 0x47 };

    ResponseEntity<String> respuesta = controlador.subirEscudo(contenido, "escudo.png", request);

    assertThat(respuesta.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR)); //significa que no se pudo guardar el escudo
    assertThat(respuesta.getBody(), is("No se encontró el directorio para guardar los escudos."));
  }

  @Test
  public void deberiaInformarSiNoPuedeGuardarElArchivo() throws IOException {
    Path archivo = directorioTemporal.resolve("no-es-un-directorio");
    Files.createFile(archivo);

    when(servletContext.getRealPath("/resources/core/img/escudos")).thenReturn(archivo.toString());

    byte[] contenido = { (byte) 0x89, 0x50, 0x4E, 0x47 };

    ResponseEntity<String> respuesta = controlador.subirEscudo(contenido, "escudo.png", request);

    assertThat(respuesta.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThat(respuesta.getBody(), is("No se pudo guardar el escudo."));
  }
}
