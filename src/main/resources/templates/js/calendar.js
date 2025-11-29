// Lógica del calendario separada de main.js
// Se auto‑inicializa solo si encuentra los elementos necesarios

document.addEventListener('DOMContentLoaded', () => {
  const monthEl = document.getElementById('fabcalMonth');
  const daysEl = document.getElementById('fabcalDays');
  const prevBtn = document.getElementById('fabcalPrev');
  const nextBtn = document.getElementById('fabcalNext');
  if (!monthEl || !daysEl || !prevBtn || !nextBtn) return; // Página sin calendario

  const meses = [
    'enero','febrero','marzo','abril','mayo','junio',
    'julio','agosto','septiembre','octubre','noviembre','diciembre'
  ];

  let view = new Date();
  view.setDate(1);
  let selected = new Date();

  function sameDay(a,b){
    return a && b && a.getFullYear()===b.getFullYear() && a.getMonth()===b.getMonth() && a.getDate()===b.getDate();
  }

  function formatDateLocal(d) {
    const y = d.getFullYear();
    const m = d.getMonth() + 1;
    const day = d.getDate();
    return y + '-' + (m < 10 ? '0' + m : m) + '-' + (day < 10 ? '0' + day : day);
  }

  function render(){
    const y = view.getFullYear();
    const m = view.getMonth();
    monthEl.textContent = meses[m].charAt(0).toUpperCase()+meses[m].slice(1)+' '+y;
    daysEl.innerHTML = '';
    const first = new Date(y,m,1);
    const last = new Date(y,m+1,0);
    const startOffset = (first.getDay()+6)%7; // Lunes=0
    for(let i=0;i<startOffset;i++) daysEl.appendChild(document.createElement('div'));
    for(let d=1; d<= last.getDate(); d++){
      const btn = document.createElement('button');
      btn.type='button';
      btn.className='day btn btn-light border-0 p-0';
      btn.textContent=d;
      const thisDate = new Date(y,m,d);
      if (sameDay(thisDate, selected)) btn.classList.add('selected');
      btn.addEventListener('click', () => {
        selected = thisDate;
        render();
        try {
          // Emitir evento custom con timestamp (más robusto que serializar Date)
          const payload = { ts: thisDate.getTime(), iso: formatDateLocal(thisDate) };
          console.debug('calendar: dispatch daySelected', payload);
          const ev = new CustomEvent('fablab:daySelected', { detail: payload });
          document.dispatchEvent(ev);
        } catch (e) {
          console.warn('calendar: error dispatching event', e);
        }
      });
      daysEl.appendChild(btn);
    }
  }

  prevBtn.addEventListener('click', () => { view.setMonth(view.getMonth()-1); render(); });
  nextBtn.addEventListener('click', () => { view.setMonth(view.getMonth()+1); render(); });

  render();
});
