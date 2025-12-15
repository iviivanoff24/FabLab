function getParam(name) {
    const url = new URL(window.location.href);
    return url.searchParams.get(name);
}
async function findImageUrl(id) {
    const bases = [`.jpg`, `.png`, `.gif`];
    for (const ext of bases) {
        const url = `/img/upload/machines/machine-${id}${ext}`;
        try {
            const r = await fetch(url, { method: "HEAD" });
            if (r.ok) return url;
        } catch {}
    }
    return "/img/logo.png";
}
document.addEventListener("DOMContentLoaded", async () => {
    const id = getParam("id");
    const titleSpan = document.getElementById("machineIdSpan");
    if (id) {
        titleSpan.textContent = `#${id}`;
    }

    // Guardia admin (cliente)
    try {
        const r = await fetch("/api/session/me");
        if (r.ok) {
            const data = await r.json();
            if (!data.admin) {
                document.getElementById("editForm").classList.add("d-none");
                document.getElementById("adminGuard").classList.remove("d-none");
            }
        }
    } catch {}

    // Setear action del formulario
    const form = document.getElementById("machineForm");
    if (id) form.action = `/admin/machines/${id}`;

    // Cargar datos actuales y pre-rellenar
    const img = document.getElementById("currentImage");
    if (id) {
        try {
            const resp = await fetch(`/api/machines/${id}`);
            if (resp.ok) {
                const data = await resp.json();
                if (img) img.src = data.imageUrl || (await findImageUrl(id));
                const name = document.getElementById("name");
                const location = document.getElementById("location");
                const description = document.getElementById("description");
                const hourlyPrice = document.getElementById("hourlyPrice");
                const status = document.getElementById("status");
                if (name && data.name) name.value = data.name;
                if (location && data.location) location.value = data.location;
                if (description && data.description)
                    description.value = data.description;
                if (hourlyPrice && data.hourlyPrice != null)
                    hourlyPrice.value = data.hourlyPrice;
                if (status && data.status) status.value = data.status;
            } else {
                if (img) img.src = await findImageUrl(id);
            }
        } catch {
            if (img) img.src = await findImageUrl(id);
        }
    }

    // Validación simple
    if (form) {
        form.addEventListener("submit", (e) => {
            form.classList.add("was-validated");
        });
    }
});
