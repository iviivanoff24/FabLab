function updateMaxStock(selectElement) {
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const stock = selectedOption.getAttribute('data-stock');
    const image1 = selectedOption.getAttribute('data-image');
    const image2 = selectedOption.getAttribute('data-image2');
    const quantityInput = selectElement.nextElementSibling; 
    const addToCartBtn = quantityInput.nextElementSibling;

    // Manejo de la opción por defecto "Opciones"
    if (selectElement.value === "") {
        // Deshabilitar controles de compra
        quantityInput.disabled = true;
        if (addToCartBtn) addToCartBtn.disabled = true;
        quantityInput.value = 1;
        quantityInput.removeAttribute('max');

        // Restaurar carrusel original
        const card = selectElement.closest('.product-card');
        if (card) {
            const carousel = card.querySelector('.carousel');
            const imgContainer = card.querySelector('.product-image-container');

            if (carousel) {
                const carouselInner = carousel.querySelector('.carousel-inner');
                const prevBtn = carousel.querySelector('.carousel-control-prev');
                const nextBtn = carousel.querySelector('.carousel-control-next');
                
                // Recuperar contenido original si existe
                const originalContent = carouselInner.getAttribute('data-original-content');
                if (originalContent) {
                    carouselInner.innerHTML = originalContent;
                    
                    // Restaurar botones
                    const itemCount = carouselInner.querySelectorAll('.carousel-item').length;
                    if (itemCount > 1) {
                        if(prevBtn) prevBtn.classList.remove('d-none');
                        if(nextBtn) nextBtn.classList.remove('d-none');
                    }
                    
                    // Reiniciar ciclo automático
                    let carouselInstance = bootstrap.Carousel.getInstance(carousel);
                    if (!carouselInstance) {
                        carouselInstance = new bootstrap.Carousel(carousel);
                    }
                    carouselInstance.cycle();
                }
            } else {
                // Restaurar imagen estática
                const staticImg = imgContainer.querySelector('img');
                if (staticImg) {
                    const originalSrc = staticImg.getAttribute('data-original-src');
                    if (originalSrc) staticImg.src = originalSrc;
                }
            }
        }
        return;
    }

    // Habilitar controles si es una selección válida
    quantityInput.disabled = false;
    if (addToCartBtn) addToCartBtn.disabled = false;
    
    // Actualizar stock máximo
    if (stock) {
        quantityInput.max = stock;
        if (parseInt(quantityInput.value) > parseInt(stock)) {
            quantityInput.value = stock;
        }
    } else {
        quantityInput.removeAttribute('max');
    }

    // Lógica de actualización de imágenes
    const card = selectElement.closest('.product-card');
    if (!card) return;

    const carousel = card.querySelector('.carousel');
    const imgContainer = card.querySelector('.product-image-container');
    
    // Recolectar imágenes válidas del subproducto
    const subProductImages = [image1, image2].filter(url => url && url.trim() !== '');

    if (carousel) {
        const carouselInner = carousel.querySelector('.carousel-inner');
        const prevBtn = carousel.querySelector('.carousel-control-prev');
        const nextBtn = carousel.querySelector('.carousel-control-next');

        // Guardar contenido original si no existe
        if (!carouselInner.hasAttribute('data-original-content')) {
            carouselInner.setAttribute('data-original-content', carouselInner.innerHTML);
        }

        let carouselInstance = bootstrap.Carousel.getInstance(carousel);
        if (!carouselInstance) {
            carouselInstance = new bootstrap.Carousel(carousel);
        }

        if (subProductImages.length > 0) {
            // Reemplazar contenido del carrusel con imágenes del subproducto
            let newHtml = '';
            subProductImages.forEach((img, index) => {
                const activeClass = index === 0 ? 'active' : '';
                newHtml += `<div class="carousel-item h-100 ${activeClass}">
                                <img src="${img}" class="d-block w-100 h-100" alt="Subproduct Image">
                            </div>`;
            });
            carouselInner.innerHTML = newHtml;

            // Gestionar visibilidad de controles
            if (subProductImages.length > 1) {
                if(prevBtn) prevBtn.classList.remove('d-none');
                if(nextBtn) nextBtn.classList.remove('d-none');
                // Si hay más de una imagen, permitimos navegar pero pausamos el auto-ciclo si se desea
                carouselInstance.pause(); 
            } else {
                if(prevBtn) prevBtn.classList.add('d-none');
                if(nextBtn) nextBtn.classList.add('d-none');
                carouselInstance.pause();
            }
            
            // Reiniciar al primer slide
            carouselInstance.to(0);

        } else {
            // Restaurar imágenes originales si el subproducto no tiene imágenes específicas
            const originalContent = carouselInner.getAttribute('data-original-content');
            if (originalContent) {
                carouselInner.innerHTML = originalContent;
                
                // Restaurar visibilidad de botones basado en cantidad original
                const itemCount = carouselInner.querySelectorAll('.carousel-item').length;
                if (itemCount > 1) {
                    if(prevBtn) prevBtn.classList.remove('d-none');
                    if(nextBtn) nextBtn.classList.remove('d-none');
                } else {
                    if(prevBtn) prevBtn.classList.add('d-none');
                    if(nextBtn) nextBtn.classList.add('d-none');
                }
                
                // Volver al inicio
                carouselInstance.to(0);
                // Opcional: reanudar ciclo si era el comportamiento original
                // carouselInstance.cycle(); 
            }
        }
    } else {
        // Caso sin carrusel (imagen estática)
        const staticImg = imgContainer.querySelector('img');
        if (staticImg) {
            // Guardar src original
            if (!staticImg.hasAttribute('data-original-src')) {
                staticImg.setAttribute('data-original-src', staticImg.src);
            }

            if (subProductImages.length > 0) {
                staticImg.src = subProductImages[0];
            } else {
                // Restaurar original
                const originalSrc = staticImg.getAttribute('data-original-src');
                if (originalSrc) staticImg.src = originalSrc;
            }
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const selects = document.querySelectorAll('.product-variant-select');
    selects.forEach(select => {
        // Forzar actualización inicial para manejar el estado "Opciones"
        updateMaxStock(select);
    });
});

// Event delegation para manejar el envío del formulario
document.addEventListener('submit', function(e) {
    if (e.target && e.target.classList.contains('add-to-cart-form')) {
        e.preventDefault(); // Prevenir el envío normal del formulario
        
        const form = e.target;
        const formData = new FormData(form);
        
        fetch('/cart/api/add', {
            method: 'POST',
            body: formData
        })
        .then(response => response.text())
        .then(data => {
            if (data === 'ok') {
                const toastEl = document.getElementById('cartToast');
                if (toastEl) {
                    const toast = new bootstrap.Toast(toastEl);
                    toast.show();
                }
            } else if (data === 'error:login') {
                window.location.href = '/login';
            } else {
                alert('Error al añadir al carrito: ' + data);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ha ocurrido un error al procesar la solicitud.');
        });
    }
});

// Filtro de búsqueda cliente para productos
document.addEventListener('DOMContentLoaded', function(){
    var input = document.getElementById('productSearch');
    var typeSelect = document.getElementById('productTypeFilter');
    if(!input || !typeSelect) return;
    
    var cards = function(){ return Array.from(document.querySelectorAll('.product-card')); };
    var normalize = function(s){ return (s||'').toString().toLowerCase().trim(); };
    
    var applyFilter = function(){
        var q = normalize(input.value);
        var t = typeSelect.value; 
        
        cards().forEach(function(card){
            var name = normalize(card.getAttribute('data-name'));
            var type = card.getAttribute('data-type'); 
            
            var matchName = q === '' || name.indexOf(q) !== -1;
            var matchType = t === '' || type === t;
            
            if (matchName && matchType) {
                card.classList.remove('d-none');
            } else {
                card.classList.add('d-none');
            }
        });
    };
    
    input.addEventListener('input', applyFilter);
    typeSelect.addEventListener('change', applyFilter);
});
