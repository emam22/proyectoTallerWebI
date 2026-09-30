
document.addEventListener("DOMContentLoaded", function () {
  var boton = document.getElementById("btnMenuUsuario");
  var opciones = document.getElementById("menuUsuarioOpciones");

  if (!boton || !opciones) {
    return;
  }

  boton.addEventListener("click", function (evento) {
    evento.stopPropagation();
    opciones.classList.toggle("abierto");
  });

  document.addEventListener("click", function () {
    opciones.classList.remove("abierto");
  });
});
