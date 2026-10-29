/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import { JSDOM } from "jsdom";
import {
  crearLetras,
  dividirEnLetras,
  iniciarAnimacionGoleadores,
  suavizar,
} from "../goleadores.js";

const ANCHO_LETRA = 8;
const SEPARACION_LETRAS = 10;

function crearEscenario() {
  const html = `
    <table>
      <tbody>
        <tr class="fila-goleador">
          <td>
            <span class="pista-nombre" data-pista>
              <span class="pelota" data-pelota></span>
              <span class="pelota-rastro" data-rastro></span>
              <span class="nombre-animado" data-letras>Lionel</span>
            </span>
          </td>
          <td>
            <span class="nombre-animado nombre-animado-apellido" data-letras-apellido>Messi</span>
          </td>
        </tr>
      </tbody>
    </table>
    <button type="button" id="btnRepetirAnimacion"></button>
  `;

  const dom = new JSDOM(html);
  const documento = dom.window.document;

  dom.window.Element.prototype.getBoundingClientRect = function () {
    if (this.classList.contains("letra")) {
      const indice = Array.from(this.parentNode.children).indexOf(this);
      const izquierda = indice * SEPARACION_LETRAS;

      return {
        left: izquierda,
        right: izquierda + ANCHO_LETRA,
        top: 0,
        bottom: 22,
        width: ANCHO_LETRA,
        height: 22,
      };
    }

    if (this.classList.contains("pelota")) {
      const traslacion = /translateX\((-?[0-9.]+)px\)/.exec(this.style.transform);
      const izquierda = traslacion ? parseFloat(traslacion[1]) : 0;

      return {
        left: izquierda,
        right: izquierda + 20,
        top: 0,
        bottom: 20,
        width: 20,
        height: 20,
      };
    }

    return {
      left: 0,
      right: 300,
      top: 0,
      bottom: 26,
      width: 300,
      height: 26,
    };
  };

  return { dom, documento };
}

function crearVentanaFalsa(movimientoReducido = false) {
  const estado = { tiempo: 0, fotogramas: [], temporizadores: [] };

  return {
    estado,
    matchMedia: () => ({ matches: movimientoReducido }),
    performance: { now: () => estado.tiempo },
    requestAnimationFrame: (funcion) => {
      estado.fotogramas.push(funcion);
      return estado.fotogramas.length;
    },
    cancelAnimationFrame: () => {},
    setTimeout: (funcion) => {
      estado.temporizadores.push(funcion);
      return estado.temporizadores.length;
    },
    clearTimeout: () => {},
    ejecutarTemporizadores: () => {
      const funciones = estado.temporizadores.splice(0, estado.temporizadores.length);
      funciones.forEach((funcion) => funcion());
    },
    avanzar: (milisegundos) => {
      estado.tiempo += milisegundos;
      const funcion = estado.fotogramas.shift();
      if (funcion) {
        funcion(estado.tiempo);
      }
    },
  };
}

describe("Goleadores", function () {
  it("debe dividir el nombre en letras", function () {
    expect(dividirEnLetras("Lionel")).toEqual(["L", "i", "o", "n", "e", "l"]);
  });

  it("debe reemplazar los espacios por espacios inseparables", function () {
    expect(dividirEnLetras("Juan Perez")).toEqual([
      "J",
      "u",
      "a",
      "n",
      " ",
      "P",
      "e",
      "r",
      "e",
      "z",
    ]);
  });

  it("debe devolver un arreglo vacio cuando el texto es null, vacio o solo espacios", function () {
    expect(dividirEnLetras(null)).toEqual([]);
    expect(dividirEnLetras("")).toEqual([]);
    expect(dividirEnLetras("   ")).toEqual([" ", " ", " "]);
  });

  it("debe suavizar el avance de la animacion", function () {
    expect(suavizar(0)).toBe(0);
    expect(suavizar(1)).toBe(1);
    expect(suavizar(2)).toBe(1);
    expect(suavizar(-1)).toBe(0);
  });

  it("debe suavizar mas rapido al principio que al final", function () {
    expect(suavizar(0.5)).toBeGreaterThan(0.5);
    expect(suavizar(0.9)).toBeGreaterThan(suavizar(0.6));
  });

  it("debe crear un span por cada letra dentro del contenedor", function () {
    const dom = new JSDOM("<div id='contenedor'>Lionel</div>");
    const documento = dom.window.document;
    const contenedor = documento.getElementById("contenedor");

    const letras = crearLetras(contenedor, "Lionel", documento);

    expect(letras.length).toBe(6);
    expect(contenedor.querySelectorAll(".letra").length).toBe(6);
    expect(contenedor.textContent).toBe("Lionel");
  });

  it("debe separar el nombre y el apellido en letras para animarlos", function () {
    const { documento } = crearEscenario();
    const ventana = crearVentanaFalsa();

    iniciarAnimacionGoleadores(documento, ventana);

    expect(documento.querySelectorAll("[data-letras] .letra").length).toBe(6);
    expect(documento.querySelectorAll("[data-letras-apellido] .letra").length).toBe(5);
  });

  it("debe revelar las letras de a medida que la pelota avanza", function () {
    const { documento } = crearEscenario();
    const ventana = crearVentanaFalsa();

    iniciarAnimacionGoleadores(documento, ventana);
    ventana.ejecutarTemporizadores();

    ventana.avanzar(100);

    expect(documento.querySelectorAll(".letra.visible").length).toBe(0);

    ventana.avanzar(200);
    const reveladas = documento.querySelectorAll("[data-letras] .letra.visible").length;

    expect(reveladas).toBe(1);
  });

  it("debe revelar todas las letras cuando la animacion termina", function () {
    const { documento } = crearEscenario();
    const ventana = crearVentanaFalsa();

    iniciarAnimacionGoleadores(documento, ventana);
    ventana.ejecutarTemporizadores();

    for (let paso = 0; paso < 20; paso++) {
      ventana.avanzar(100);
    }

    expect(documento.querySelectorAll(".letra.visible").length).toBe(11);
  });

  it("debe ocultar las letras y volver a animarlas cuando se repite", function () {    const { documento } = crearEscenario();
    const ventana = crearVentanaFalsa();

    iniciarAnimacionGoleadores(documento, ventana);
    ventana.ejecutarTemporizadores();

    for (let paso = 0; paso < 20; paso++) {
      ventana.avanzar(100);
    }

    expect(documento.querySelectorAll(".letra.visible").length).toBe(11);

    documento.getElementById("btnRepetirAnimacion").click();

    expect(documento.querySelectorAll(".letra.visible").length).toBe(0);
    expect(ventana.estado.temporizadores.length).toBe(1);
  });

  it("debe mostrar el nombre completo sin movimiento si se pide reducirlo", function () {
    const { documento } = crearEscenario();
    const ventana = crearVentanaFalsa(true);

    iniciarAnimacionGoleadores(documento, ventana);

    expect(documento.querySelectorAll(".letra.visible").length).toBe(11);
    expect(ventana.estado.fotogramas.length).toBe(0);
  });

  it("no debe hacer nada si la vista no tiene la pista del goleador", function () {
    const dom = new JSDOM("<div></div>");
    const ventana = crearVentanaFalsa();

    expect(iniciarAnimacionGoleadores(dom.window.document, ventana)).toBeNull();
  });
});
