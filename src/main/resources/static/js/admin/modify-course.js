function getParam(name) { const url=new URL(window.location.href); return url.searchParams.get(name); }
async function findImageUrl(id){ const bases=[`.jpg`,`.png`,`.gif`]; for(const ext of bases){ const url=`/img/upload/courses/course-${id}${ext}`; try{ const r=await fetch(url,{method:'HEAD'}); if(r.ok) return url;}catch{} } return '/img/logo.png'; }
document.addEventListener('DOMContentLoaded', async ()=>{
  const id = getParam('id');
  const titleSpan = document.getElementById('courseIdSpan'); if (id) titleSpan.textContent = `#${id}`;
  try{ const r = await fetch('/api/session/me'); if (r.ok) { const d = await r.json(); if(!d.admin){ document.getElementById('editForm').classList.add('d-none'); document.getElementById('adminGuard').classList.remove('d-none'); } } } catch{}
  const form = document.getElementById('courseForm'); if (id) form.action = `/admin/courses/${id}`;
  const img = document.getElementById('currentImage'); if (id) {
    try{
      // Obtener datos desde la API 
      const resp = await fetch(`/api/courses/${id}`);
      if (resp.ok){
        const data = await resp.json();
        if(img) img.src = data.imageUrl || await findImageUrl(id);
        const title = document.getElementById('title');
        const description = document.getElementById('description');
        const startDate = document.getElementById('startDate');
        const endDate = document.getElementById('endDate');
        const capacity = document.getElementById('capacity');
        const price = document.getElementById('price');
        if(title && data.name) title.value = data.name;
        if(description && data.description) description.value = data.description;
        if(startDate && data.startDate) startDate.value = data.startDate;
        if(endDate && data.endDate) endDate.value = data.endDate;
        if(capacity && data.capacity != null) capacity.value = data.capacity;
        // Precio puede venir en 'precio' o 'price' según el DTO; manejar ambos
        if(price) {
          if(data.precio != null) price.value = data.precio;
          else if(data.price != null) price.value = data.price;
        }
      } else {
        if(img) img.src = await findImageUrl(id);
      }
    } catch(err){
      if(img) img.src = await findImageUrl(id);
    }
  }
  if (form) {
      form.addEventListener('submit', e => { form.classList.add('was-validated'); });
  }
});
