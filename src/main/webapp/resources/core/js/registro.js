document.addEventListener("DOMContentLoaded", () => {
  const btnComprarAdmin = document.getElementById("btn-comprar-admin");

  if (btnComprarAdmin) {
    btnComprarAdmin.addEventListener("click", () => {
      const emailInput = document.getElementById("email").value.trim();
      const passInput = document.getElementById("password").value.trim();

      if (!emailInput || !passInput) {
        alert("Por favor completa tu email y contraseña antes de continuar con la compra.");
        return;
      }

      const params = new URLSearchParams({
        email: emailInput,
        password: passInput
      });

      window.location.href = "/spring/pagar-inscripcion?" + params.toString();
    });
  }
});
