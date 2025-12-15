document.addEventListener('DOMContentLoaded', () => {
    const quantityInputs = document.querySelectorAll('input[name="quantity"]');
    quantityInputs.forEach(input => {
        input.addEventListener('change', function() {
            this.form.submit();
        });
    });
});
