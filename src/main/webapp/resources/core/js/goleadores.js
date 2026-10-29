/**
 * Animacion de la tabla de goleadores.
 * La pelota recorre el nombre del goleador y revela cada letra a su paso.
 * La tabla se muestra igual si este archivo no llega a cargarse:
 * las letras solo se ocultan cuando la fila se marca como "animando".
 */

const DURACION_ANIMACION = 1650;
const RETARDO_INICIO = 320;
const ESPACIO_INSEPARABLE = "\u00a0";

export function dividirEnLetras(texto) {
  if (!texto) {
    return [];
  }

  return Array.from(texto).map((caracter) =>
    caracter === " " ? ESPACIO_INSEPARABLE : caracter
  );
}

export function suavizar(progreso) {
  const limitado = Math.min(Math.max(progreso, 0), 1);

  return 1 - Math.pow(1 - limitado, 1.65);
}

export function crearLetras(contenedor, texto, documento) {
  const letras = dividirEnLetras(texto);

  contenedor.textContent = "";

  return letras.map((letra) => {
    const span = documento.createElement("span");
    span.className = "letra";
    span.textContent = letra;
    contenedor.appendChild(span);

    return span;
  });
}

export function iniciarAnimacionGoleadores(documento = document, ventana = window) {
  const pista = documento.querySelector("[data-pista]");

  if (!pista) {
    return null;
  }

  const contenedorNombre = pista.querySelector("[data-letras]");
  const contenedorApellido = documento.querySelector("[data-letras-apellido]");
  const pelota = pista.querySelector("[data-pelota]");
  const rastro = pista.querySelector("[data-rastro]");
  const boton = documento.getElementById("btnRepetirAnimacion");
  const fila = pista.closest("tr");

  if (!contenedorNombre || !pelota) {
    return null;
  }

  const letras = crearLetras(contenedorNombre, contenedorNombre.textContent, documento);
  const letrasApellido = contenedorApellido
    ? crearLetras(contenedorApellido, contenedorApellido.textContent, documento)
    : [];
  const todasLasLetras = letras.concat(letrasApellido);

  let cuadro = 0;
  let temporizador = 0;
  let generacion = 0;

  function limpiar() {
    generacion++;
    ventana.cancelAnimationFrame(cuadro);
    ventana.clearTimeout(temporizador);

    todasLasLetras.forEach((letra) => letra.classList.remove("visible"));

    pelota.style.opacity = "0";
    pelota.style.transform = "none";

    if (rastro) {
      rastro.style.opacity = "0";
      rastro.style.transform = "none";
    }
  }

  function revelarTodas() {
    todasLasLetras.forEach((letra) => letra.classList.add("visible"));
  }

  function animar(token) {
    if (token !== generacion || letras.length === 0) {
      return;
    }

    const anchoPelota = pelota.getBoundingClientRect().width;
    const cajaPista = pista.getBoundingClientRect();
    const primeraLetra = letras[0].getBoundingClientRect();
    const ultimaLetra = letras[letras.length - 1].getBoundingClientRect();
    const inicioX = primeraLetra.left - cajaPista.left - anchoPelota - 5;
    const finX = ultimaLetra.right - cajaPista.left + 7;
    const tiempoInicio = ventana.performance.now();
    let centroAnterior = -Infinity;

    pelota.style.opacity = "1";

    function paso(ahora) {
      if (token !== generacion) {
        return;
      }

      const progreso = Math.min((ahora - tiempoInicio) / DURACION_ANIMACION, 1);
      const x = inicioX + (finX - inicioX) * suavizar(progreso);

      pelota.style.transform = `translateX(${x}px) rotate(${progreso * 660}deg)`;

      if (rastro) {
        rastro.style.opacity = progreso > 0.03 && progreso < 0.95 ? "0.65" : "0";
        rastro.style.transform = `translateX(${x - 14}px)`;
      }

      const cajaPelota = pelota.getBoundingClientRect();
      const centro = cajaPelota.left + cajaPelota.width / 2;

      letras.forEach((letra) => {
        const caja = letra.getBoundingClientRect();
        const centroLetra = caja.left + caja.width / 2;

        if (
          !letra.classList.contains("visible") &&
          centroAnterior < centroLetra &&
          centro >= centroLetra
        ) {
          letra.classList.add("visible");
        }
      });

      centroAnterior = centro;

      if (progreso < 1) {
        cuadro = ventana.requestAnimationFrame(paso);
      } else {
        revelarTodas();
      }
    }

    cuadro = ventana.requestAnimationFrame(paso);
  }

  function reproducir() {
    limpiar();

    const token = generacion;

    if (fila) {
      fila.classList.add("animando");
    }

    if (ventana.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      revelarTodas();
      return;
    }

    temporizador = ventana.setTimeout(() => animar(token), RETARDO_INICIO);
  }

  if (boton) {
    boton.addEventListener("click", reproducir);
  }

  reproducir();

  return { reproducir, limpiar };
}

if (typeof document !== "undefined") {
  iniciarAnimacionGoleadores();
}
