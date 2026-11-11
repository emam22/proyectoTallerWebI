document.addEventListener("DOMContentLoaded", () => {
  const btnComprarAdmin = document.getElementById("btn-comprar-admin");

  if (btnComprarAdmin) {
    btnComprarAdmin.addEventListener("click", async (e) => {
      e.preventDefault();

      const emailInput = document.getElementById("email") ? document.getElementById("email").value.trim() : "";
      const passInput = document.getElementById("password") ? document.getElementById("password").value : "";

      if (!emailInput || !passInput) {
        alert("Por favor completa tu email y contraseña antes de continuar con la compra.");
        return;
      }

      try {
        const response = await fetch("/spring/api/validar-password?password=" + encodeURIComponent(passInput));
        const data = await response.json();

        if (!data.valido) {
          alert(data.mensaje);
          return; // Frena acá si es débil, no va a Mercado Pago
        }

        const params = new URLSearchParams({
          email: emailInput,
          password: passInput
        });

        window.location.href = "/spring/pagar-inscripcion?" + params.toString();

      } catch (err) {
        console.error("Error al validar contraseña:", err);
        alert("Error al verificar la contraseña.");
      }
    });
  }
});