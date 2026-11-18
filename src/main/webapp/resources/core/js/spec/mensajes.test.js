import { JSDOM } from "jsdom";

const dom = new JSDOM("<!doctype html><html><body></body></html>");

globalThis.window = dom.window;
globalThis.document = dom.window.document;

await import("../mensajes.js");

describe("MensajesProyecto", () => {
  afterEach(() => {
    document.querySelector("[data-mensaje-proyecto=\"true\"]")?.remove();
    document.body.innerHTML = "";
  });

  it("muestra el mensaje en un modal con el estilo de error del proyecto", () => {
    window.MensajesProyecto.mostrar("Completá el nombre", "error");

    const modal = document.querySelector("[data-mensaje-proyecto=\"true\"]");
    const dialog = modal.querySelector(".modal-confirmacion__dialog");

    expect(dialog.classList).toContain("modal-confirmacion__dialog--error");
    expect(modal.querySelector(".modal-confirmacion__mensaje").textContent).toBe(
      "Completá el nombre"
    );
  });

  it("cierra el modal al hacer clic en Aceptar", () => {
    window.MensajesProyecto.mostrar("Listo", "ok");

    document.querySelector(".btn-modal--confirmar").click();

    expect(document.querySelector("[data-mensaje-proyecto=\"true\"]")).toBeNull();
  });

  it("enumera los campos obligatorios que faltan y frena el submit", () => {
    document.body.innerHTML = `
      <form>
        <label for="nombre">Nombre</label>
        <input id="nombre" required>
        <button type="submit">Guardar</button>
      </form>
    `;
    const formulario = document.querySelector("form");
    window.MensajesProyecto.configurarValidacion(formulario);

    const evento = new dom.window.Event("submit", { bubbles: true, cancelable: true });
    formulario.dispatchEvent(evento);

    expect(evento.defaultPrevented).toBeTrue();
    expect(document.querySelector(".modal-confirmacion__mensaje").textContent).toContain(
      "Completá los campos obligatorios:\n• Nombre"
    );
  });

  it("deja enviar el formulario cuando los campos estan completos", () => {
    document.body.innerHTML = `
      <form>
        <label for="nombre">Nombre</label>
        <input id="nombre" required value="Torneo">
        <button type="submit">Guardar</button>
      </form>
    `;
    const formulario = document.querySelector("form");
    window.MensajesProyecto.configurarValidacion(formulario);

    const evento = new dom.window.Event("submit", { bubbles: true, cancelable: true });
    formulario.dispatchEvent(evento);

    expect(evento.defaultPrevented).toBeFalse();
    expect(document.querySelector("[data-mensaje-proyecto=\"true\"]")).toBeNull();
  });
});
