/**
 * js/autoplay-carousel.js
 * Gestiona la transición automática y la interacción manual del carrusel 3D basado en radio buttons.
 */

document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.carousel-container-3d');
    if (!container) return; 

    const radioInputs = container.querySelectorAll('input[type="radio"][name="position"]');
    const clickableItems = container.querySelectorAll('.clickable-item');
    const totalSlides = radioInputs.length;
    let currentSlide = 0; 
    let autoplayInterval;

    const intervalTime = 3000; // 3 segundos

    // Función para avanzar al siguiente slide
    function nextSlide() {
        // Calcular el índice del siguiente slide (cíclico)
        const nextIndex = (currentSlide + 1) % totalSlides;
        
        // Simular el clic en el botón de radio
        if (radioInputs[nextIndex]) {
            radioInputs[nextIndex].click();
            currentSlide = nextIndex;
        }
    }

    // Función para iniciar/reiniciar el autoplay
    function startAutoplay() {
        clearInterval(autoplayInterval);
        autoplayInterval = setInterval(nextSlide, intervalTime);
    }

    // --- 1. Manejar el clic en las fotos (div.item) ---
    clickableItems.forEach((item, index) => {
        item.addEventListener('click', () => {
            // Si ya es el slide principal, no hacemos nada 
            if (index === currentSlide) return; 

            // Actualizar el índice actual y simular el clic en el radio button
            if (radioInputs[index]) {
                currentSlide = index;
                // Usamos .click() para asegurar que el evento cambie el estado y active el CSS
                radioInputs[index].click(); 
            }
            
            // Reiniciar el autoplay después de la interacción manual:
            startAutoplay(); 
        });
    });
    
    // --- 2. Manejar el clic en los radio buttons (para reiniciar el timer) ---
    radioInputs.forEach((radio, index) => {
        radio.addEventListener('click', () => {
            // Actualizar el índice actual (ya que el usuario lo cambió)
            currentSlide = index;
            // Reiniciar el autoplay después de la interacción manual:
            startAutoplay();
        });
    });

    // Iniciar el carrusel al cargar la página
    startAutoplay();
});