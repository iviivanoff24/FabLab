function updateMaxStock(selectElement) {
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const stock = selectedOption.getAttribute('data-stock');
    const quantityInput = selectElement.nextElementSibling; 
    
    if (stock) {
        quantityInput.max = stock;
        if (parseInt(quantityInput.value) > parseInt(stock)) {
            quantityInput.value = stock;
        }
    } else {
        quantityInput.removeAttribute('max');
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const selects = document.querySelectorAll('.product-variant-select');
    selects.forEach(select => {
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
