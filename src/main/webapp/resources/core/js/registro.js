document.addEventListener("DOMContentLoaded", () => {
  const btnComprarAdmin = document.getElementById("btn-comprar-admin");

  if (btnComprarAdmin) {
    btnComprarAdmin.addEventListener("click", async (e) => {
      e.preventDefault();

      const emailInput = document.getElementById("email") ? document.getElementById("email").value.trim() : "";
      const passInput = document.getElementById("password") ? document.getElementById("password").value : "";

      if (!emailInput || !passInput) {
        alert("Por favor completa tu email y contrase\u00f1a antes de continuar con la compra.");
        return;
      }

      try {
        const respuestaValidacion = await fetch("/spring/api/validar-password", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ password: passInput })
        });
        const validacion = await respuestaValidacion.json();

        if (!validacion.valido) {
          alert(validacion.mensaje);
          return; // Frena ac\u00e1 si es d\u00e9bil, no va a Mercado Pago
        }

        const respuestaPreparacion = await fetch("/spring/api/preparar-pago", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: emailInput, password: passInput })
        });
        const preparacion = await respuestaPreparacion.json();

        if (!respuestaPreparacion.ok || preparacion.status !== "ok") {
          alert(preparacion.mensaje || "No se pudo preparar el pago.");
          return;
        }

        // El email y la contrase\u00f1a quedan en la sesi\u00f3n del servidor: no van por URL.
        window.location.href = "/spring/pagar-inscripcion";
      } catch (err) {
        console.error("Error al validar contrase\u00f1a:", err);
        alert("Error al verificar la contrase\u00f1a.");
      }
    });
  }
});
