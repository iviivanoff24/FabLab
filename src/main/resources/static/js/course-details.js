document.addEventListener("DOMContentLoaded", function () {
    // Delete Course Logic
    var confirmDeleteBtn = document.getElementById("confirmDeleteCourseBtn");
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", function () {
            var f = document.getElementById("deleteCourseForm");
            if (f) f.submit();
        });
    }

    var modal = document.getElementById("cancelInscriptionModal");
    var cancelInput = document.getElementById("cancelInscriptionId");
    var confirmBtn = document.getElementById("confirmCancelInscriptionBtn");
    if (!modal) return;
    modal.addEventListener("show.bs.modal", function (ev) {
        var btn = ev.relatedTarget;
        if (!btn) return;
        var insId = btn.getAttribute("data-inscription-id") || "";
        if (cancelInput) cancelInput.value = insId;
    });
    if (confirmBtn)
        confirmBtn.addEventListener("click", function () {
            var f = document.getElementById("cancelInscriptionForm");
            if (f) f.submit();
        });
});
