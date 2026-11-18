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
        const response = await fetch("/spring/api/validar-password?password=" + encodeURIComponent(passInput));
        const data = await response.json();

        if (!data.valido) {
          alert(data.mensaje);
          return; // Frena ac\u00e1 si es d\u00e9bil, no va a Mercado Pago
        }

        const params = new URLSearchParams({
          email: emailInput,
          password: passInput
        });

        window.location.href = "/spring/pagar-inscripcion?" + params.toString();

      } catch (err) {
        console.error("Error al validar contrase\u00f1a:", err);
        alert("Error al verificar la contrase\u00f1a.");
      }
    });
  }
});