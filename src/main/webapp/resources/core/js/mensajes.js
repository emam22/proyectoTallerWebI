(function () {
  "use strict";

  function construirModal(mensaje, tipo, alCerrar) {
    var modal = document.createElement("div");
    modal.className = "modal-confirmacion";
    modal.dataset.mensajeProyecto = "true";

    var overlay = document.createElement("div");
    overlay.className = "modal-confirmacion__overlay";

    var dialog = document.createElement("div");
    dialog.className = "modal-confirmacion__dialog";
    dialog.classList.add(
      tipo === "ok"
        ? "modal-confirmacion__dialog--ok"
        : "modal-confirmacion__dialog--error"
    );
    dialog.setAttribute("role", "alertdialog");
    dialog.setAttribute("aria-modal", "true");
    dialog.setAttribute("aria-labelledby", "mensaje-proyecto-titulo");
    dialog.setAttribute("aria-describedby", "mensaje-proyecto-detalle");

    var titulo = document.createElement("h2");
    titulo.id = "mensaje-proyecto-titulo";
    if (tipo !== "ok") {
      titulo.className = "modal-titulo--error";
    }
    titulo.textContent = tipo === "ok" ? "Listo" : "Atenci\u00f3n";

    var detalle = document.createElement("p");
    detalle.id = "mensaje-proyecto-detalle";
    detalle.className = "modal-confirmacion__mensaje";
    detalle.textContent = mensaje;

    var acciones = document.createElement("div");
    acciones.className = "modal-confirmacion__acciones";

    var aceptar = document.createElement("button");
    aceptar.type = "button";
    aceptar.className = "btn-modal btn-modal--confirmar";
    aceptar.textContent = "Aceptar";

    acciones.appendChild(aceptar);
    dialog.append(titulo, detalle, acciones);
    modal.append(overlay, dialog);
    document.body.appendChild(modal);

    function cerrar() {
      document.removeEventListener("keydown", alTecla);
      modal.remove();
      if (alCerrar) {
        alCerrar();
      }
    }

    function alTecla(evento) {
      if (evento.key === "Escape") {
        cerrar();
      }
    }

    overlay.addEventListener("click", cerrar);
    aceptar.addEventListener("click", cerrar);
    document.addEventListener("keydown", alTecla);
    aceptar.focus();
  }

  function mostrar(mensaje, tipo, alCerrar) {
    var abierto = document.querySelector("[data-mensaje-proyecto=\"true\"]");
    if (abierto) {
      abierto.remove();
    }
    construirModal(
      String(mensaje === null || mensaje === undefined ? "" : mensaje),
      tipo,
      alCerrar
    );
  }

  function nombreCampo(campo) {
    if (campo.labels && campo.labels.length > 0) {
      var etiqueta = campo.labels[0].textContent.trim();
      if (etiqueta) {
        return etiqueta;
      }
    }
    if (campo.getAttribute("placeholder")) {
      return campo.getAttribute("placeholder").trim();
    }
    if (campo.name) {
      return campo.name;
    }
    return "Campo obligatorio";
  }

  function configurarValidacion(formulario) {
    if (!formulario) {
      return;
    }

    formulario.noValidate = true;

    formulario.addEventListener("submit", function (evento) {
      var camposInvalidos = [];

      formulario.querySelectorAll("input, select, textarea").forEach(function (campo) {
        if (campo.disabled || campo.type === "hidden") {
          return;
        }
        if (typeof campo.checkValidity === "function" && !campo.checkValidity()) {
          camposInvalidos.push(campo);
        }
      });

      if (camposInvalidos.length === 0) {
        return;
      }

      evento.preventDefault();
      evento.stopImmediatePropagation();

      var detalle = camposInvalidos
        .map(nombreCampo)
        .map(function (nombre) {
          return "- " + nombre;
        })
        .join("\n");

      mostrar("Complet\u00e1 los campos obligatorios:\n" + detalle, "error", function () {
        camposInvalidos[0].focus();
      });
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("form").forEach(configurarValidacion);
  });

  window.alert = function (mensaje) {
    mostrar(mensaje, "error");
  };

  window.MensajesProyecto = {
    mostrar: mostrar,
    configurarValidacion: configurarValidacion
  };
})();
