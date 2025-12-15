(function(){
    document.addEventListener('DOMContentLoaded', function(){
        var form = document.querySelector('form');
        
        function setupPasswordToggle(toggleId, inputId) {
            const toggle = document.getElementById(toggleId);
            const input = document.getElementById(inputId);
            
            if (toggle && input) {
                toggle.addEventListener('click', function() {
                    const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                    input.setAttribute('type', type);
                    
                    const icon = this.querySelector('i');
                    if (type === 'password') {
                        icon.classList.remove('bi-eye-slash');
                        icon.classList.add('bi-eye');
                    } else {
                        icon.classList.remove('bi-eye');
                        icon.classList.add('bi-eye-slash');
                    }
                });
            }
        }

        setupPasswordToggle('toggleNewPassword', 'newPassword');
        setupPasswordToggle('toggleConfirmPassword', 'confirmPassword');

        if (!form) return;
        form.addEventListener('submit', function(e){
            var newP = document.getElementById('newPassword').value;
            var confirmP = document.getElementById('confirmPassword').value;
            if ((newP || confirmP) && newP !== confirmP) {
                e.preventDefault();
                alert('La nueva contraseña y la confirmación no coinciden.');
                return false;
            }
        });
    });
})();
