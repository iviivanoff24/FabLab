document.addEventListener('DOMContentLoaded',function(){
  var modal=document.getElementById('deleteMachineModal');
  if(modal){
    var currentId=null;var nameSpan=document.getElementById('deleteMachineName');
    modal.addEventListener('show.bs.modal',function(ev){var btn=ev.relatedTarget;currentId=btn.getAttribute('data-machine-id');var nm=btn.getAttribute('data-machine-name');nameSpan.textContent=nm||currentId;});
    var confirmBtn=document.getElementById('confirmDeleteMachineBtn');
    if(confirmBtn){confirmBtn.addEventListener('click',function(){if(currentId){var f=document.getElementById('form-delete-machine-'+currentId);if(f){f.submit();}}});}
  }

  // Filtro de búsqueda cliente para máquinas
  var input = document.getElementById('machineSearch');
  var statusSelect = document.getElementById('machineStatusFilter');
  
  if(input && statusSelect) {
      var normalize = function(s){ return (s||'').toString().toLowerCase().trim(); };
      var cards = function(){ return Array.from(document.querySelectorAll('.machine-card')); };
      
      var applyFilter = function(){
        var q = normalize(input.value);
        var s = statusSelect.value;
        
        cards().forEach(function(card){
          var name = normalize(card.getAttribute('data-name'));
          var desc = normalize(card.getAttribute('data-desc'));
          var status = card.getAttribute('data-status');
          
          var matchName = q === '' || name.indexOf(q) !== -1 || desc.indexOf(q) !== -1;
          var matchStatus = s === '' || status === s;
          
          if (matchName && matchStatus) {
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
  }
});
