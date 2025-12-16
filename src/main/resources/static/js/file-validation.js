document.addEventListener('DOMContentLoaded', function() {
    // Create the modal HTML dynamically if it doesn't exist
    if (!document.getElementById('fileSizeModal')) {
        const modalHtml = `
        <div class="modal fade" id="fileSizeModal" tabindex="-1" aria-labelledby="fileSizeModalLabel" aria-hidden="true">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title" id="fileSizeModalLabel">Error de archivo</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">
                La imagen seleccionada es demasiado grande. El tamaño máximo permitido es de 2MB.
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
              </div>
            </div>
          </div>
        </div>`;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
    }

    const fileInputs = document.querySelectorAll('input[type="file"]');
    const maxSizeBytes = 2 * 1024 * 1024; // 2MB

    fileInputs.forEach(input => {
        input.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                if (this.files[0].size > maxSizeBytes) {
                    // Show modal
                    const modal = new bootstrap.Modal(document.getElementById('fileSizeModal'));
                    modal.show();
                    
                    // Clear the input
                    this.value = '';
                }
            }
        });
    });
});
