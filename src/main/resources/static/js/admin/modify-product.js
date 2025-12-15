document.addEventListener('DOMContentLoaded', () => {
    const deleteProductForms = document.querySelectorAll('form[action="/admin/products/delete"]');
    deleteProductForms.forEach(form => {
        form.addEventListener('submit', e => {
            if (!confirm('¿Estás seguro de eliminar este producto?')) {
                e.preventDefault();
            }
        });
    });

    const deleteSubproductForms = document.querySelectorAll('form[action="/admin/subproducts/delete"]');
    deleteSubproductForms.forEach(form => {
        form.addEventListener('submit', e => {
            if (!confirm('¿Eliminar esta variante?')) {
                e.preventDefault();
            }
        });
    });
});
