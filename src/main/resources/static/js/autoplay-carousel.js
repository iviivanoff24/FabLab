/**
 * js/autoplay-carousel.js
 * Gestiona el cambio de tema CSS (--light) al hacer click.
 * NO gestiona la animación de rotación del carrusel, que es CSS.
 */

let CHECKED = false;
document.addEventListener("pointerdown", (e) => {
    CHECKED = !CHECKED;
    // Esto es lo que alterna el modo claro/oscuro en el ejemplo original.
    document.documentElement.style.setProperty("--light", CHECKED ? 1 : 0);
});