(function(){
    document.addEventListener('DOMContentLoaded', function(){
        var confirmModalEl = document.getElementById('confirmModal');
        if(!confirmModalEl) return;
        var confirmForm = document.getElementById('confirmForm');
        var confirmMessage = document.getElementById('confirmMessage');
        var modal = new bootstrap.Modal(confirmModalEl);

        document.querySelectorAll('.btn-delete').forEach(function(btn){
            btn.addEventListener('click', function(e){
                var action = btn.getAttribute('data-action');
                var entity = btn.getAttribute('data-entity') || 'item';
                var id = btn.getAttribute('data-id') || '';
                confirmForm.setAttribute('action', action);
                var label = id ? (entity + ' #' + id) : entity;
                confirmMessage.textContent = '¿Confirma que desea borrar ' + label + '?';
                modal.show();
            });
        });
    });
})();
