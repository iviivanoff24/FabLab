// Lógica para el modal de borrado (sin cambios)
document.addEventListener('DOMContentLoaded',function(){
  var modal=document.getElementById('deleteCourseModal');
  if(!modal) return;
  var currentId=null; var nameSpan=document.getElementById('deleteCourseName');
  modal.addEventListener('show.bs.modal',function(ev){ var btn=ev.relatedTarget; currentId=btn.getAttribute('data-course-id'); var nm=btn.getAttribute('data-course-name'); nameSpan.textContent=nm||currentId; });
  var confirmBtn=document.getElementById('confirmDeleteCourseBtn');
  if(confirmBtn){ confirmBtn.addEventListener('click',function(){ if(currentId){ var f=document.getElementById('form-delete-course-'+currentId); if(f){ f.submit(); } } }); }
});

document.addEventListener('DOMContentLoaded', function(){
    document.querySelectorAll('.duration-badge').forEach(function(el){
        try{
            var s = el.getAttribute('data-start');
            var e = el.getAttribute('data-end');
            if(!s || !e) { el.querySelector('.duration-text').textContent = '-'; return; }
            var ds = new Date(s);
            var de = new Date(e);
            if(isNaN(ds) || isNaN(de)) { el.querySelector('.duration-text').textContent = '-'; return; }
            // contamos días completos inclusivos
            var diff = Math.round((de - ds) / (1000*60*60*24)) + 1;
            el.querySelector('.duration-text').textContent = diff + ' días';
        }catch(err){ el.querySelector('.duration-text').textContent = '-'; }
    });
});

// Filtro de búsqueda cliente para cursos
document.addEventListener('DOMContentLoaded', function(){
    var input = document.getElementById('courseSearch');
    var statusSelect = document.getElementById('courseStatusFilter');
    var priceSelect = document.getElementById('coursePriceFilter');
    
    if(!input || !statusSelect || !priceSelect) return;
    
    var cards = function(){ return Array.from(document.querySelectorAll('.course-card')); };
    var normalize = function(s){ return (s||'').toString().toLowerCase().trim(); };
    
    var applyFilter = function(){
        var q = normalize(input.value);
        var s = statusSelect.value;
        var p = priceSelect.value;
        
        cards().forEach(function(card){
            var name = normalize(card.getAttribute('data-name'));
            var desc = normalize(card.getAttribute('data-desc'));
            var status = card.getAttribute('data-status');
            var priceType = card.getAttribute('data-price-type');
            
            var matchName = q === '' || name.indexOf(q) !== -1 || desc.indexOf(q) !== -1;
            var matchStatus = s === '' || status === s;
            var matchPrice = p === '' || priceType === p;
            
            if (matchName && matchStatus && matchPrice) {
                card.classList.remove('d-none');
                card.style.display = '';
            } else {
                card.classList.add('d-none');
                card.style.display = 'none';
            }
        });
    };
    
    input.addEventListener('input', applyFilter);
    statusSelect.addEventListener('change', applyFilter);
    priceSelect.addEventListener('change', applyFilter);
});
