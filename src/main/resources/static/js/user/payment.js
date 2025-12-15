document.addEventListener("DOMContentLoaded", function () {
  function updateVisibility() {
    const pm =
      document.querySelector("input[name=pm]:checked")?.value ||
      "Tarjeta";
    document.getElementById("paymentMethod").value = pm;
    
    const cardFields = document.getElementById("cardFields");
    const onlineFields = document.getElementById("onlineFields");
    
    // Toggle visibility
    cardFields.style.display = pm === "Tarjeta" ? "" : "none";
    onlineFields.style.display = pm === "Online" ? "" : "none";

    // Toggle required attributes
    const cardInputs = cardFields.querySelectorAll("input");
    const onlineSelect = document.getElementById("onlineSelect");

    if (pm === "Tarjeta") {
      cardInputs.forEach(input => input.setAttribute("required", ""));
      if (onlineSelect) onlineSelect.removeAttribute("required");
    } else if (pm === "Online") {
      cardInputs.forEach(input => input.removeAttribute("required"));
      if (onlineSelect) onlineSelect.setAttribute("required", "");
    } else {
      // Efectivo
      cardInputs.forEach(input => input.removeAttribute("required"));
      if (onlineSelect) onlineSelect.removeAttribute("required");
    }
  }

  document
    .querySelectorAll("input[name=pm]")
    .forEach((r) => r.addEventListener("change", updateVisibility));
  
  const onlineSel = document.getElementById("onlineSelect");
  if (onlineSel)
    onlineSel.addEventListener("change", function () {
      document.getElementById("onlineProvider").value = this.value;
    });
  
  updateVisibility();
  
  const cancelBtn = document.getElementById("cancelBtn");
  if (cancelBtn) {
    cancelBtn.addEventListener("click", function () {
      const ret = this.getAttribute("data-return");
      // Usar returnUrl solo si parece una ruta local segura (empieza por '/')
      // o es una URL absoluta con el mismo origen. En caso contrario, volver atrás.
      try {
        if (ret && ret !== "null" && ret !== "") {
          // Normalizar URL relativa
          const candidate = new URL(ret, window.location.origin);
          if (
            candidate.origin === window.location.origin &&
            candidate.pathname.startsWith("/")
          ) {
            window.location.href = candidate.href;
            return;
          }
        }
      } catch (e) {
        // Si la URL es inválida, caeremos al history.back()
      }
      history.back();
    });
  }
});
