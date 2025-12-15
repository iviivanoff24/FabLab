(function(){
    function normalize(s){ return (s||'').toString().toLowerCase(); }

    function rowMatchesFilters(row, query, filters){
        if(query && normalize(row.textContent).indexOf(normalize(query)) === -1) return false;
        for(var i=0;i<filters.length;i++){
            var f = filters[i];
            var key = f.getAttribute('data-key');
            if(!key) continue;
            var val = f.value;
            if(!val) continue; // empty means 'all'
            var rowVal = row.dataset ? (row.dataset[key] || '') : '';
            if(normalize(rowVal) !== normalize(val)) return false;
        }
        return true;
    }

    function applyFilters(entity){
        var table = document.querySelector('table[data-entity="'+entity+'"]');
        if(!table) return;
        var rows = table.tBodies[0] ? Array.from(table.tBodies[0].rows) : [];
        var queryEl = document.querySelector('.table-search[data-target="'+entity+'"]');
        var q = queryEl ? queryEl.value : '';
        var filters = Array.from(document.querySelectorAll('.table-filter[data-target="'+entity+'"]'));
        rows.forEach(function(r){
            r.style.display = rowMatchesFilters(r, q, filters) ? '' : 'none';
        });
    }

    function parseISODate(s){ try{ return s ? new Date(s) : null; }catch(e){return null;} }
    function sortTableByColumn(table, colIndex, dir){
        var tbody = table.tBodies[0]; if(!tbody) return;
        var rows = Array.from(tbody.rows).filter(r=>r.style.display !== 'none');
        var sample = rows.map(r => (r.cells[colIndex] ? r.cells[colIndex].textContent.trim() : '')).find(x=>x && x.length>0) || '';
        var isDate = /^\d{4}-\d{2}-\d{2}$/.test(sample);
        var isNumber = !isDate && rows.some(r=>{ var v = r.cells[colIndex] ? r.cells[colIndex].textContent.trim().replace(/,/g,'.') : ''; return v !== '' && !isNaN(parseFloat(v)); });
        rows.sort(function(a,b){
            var va = a.cells[colIndex] ? a.cells[colIndex].textContent.trim() : '';
            var vb = b.cells[colIndex] ? b.cells[colIndex].textContent.trim() : '';
            if(isDate){ var da = parseISODate(va); var db = parseISODate(vb); da = da?da.getTime():0; db = db?db.getTime():0; return da - db; }
            if(isNumber){ var na = parseFloat(va.replace(/,/g,'.')) || 0; var nb = parseFloat(vb.replace(/,/g,'.')) || 0; return na - nb; }
            return va.localeCompare(vb, undefined, {numeric:true, sensitivity:'base'});
        });
        if(dir !== 'asc') rows.reverse();
        rows.forEach(function(r){ tbody.appendChild(r); });
    }

    document.addEventListener('DOMContentLoaded', function(){
        // Filtering
        document.querySelectorAll('.table-search').forEach(function(input){
            var target = input.getAttribute('data-target');
            input.addEventListener('input', function(){ applyFilters(target); });
        });
        document.querySelectorAll('.table-filter').forEach(function(sel){
            var target = sel.getAttribute('data-target');
            sel.addEventListener('change', function(){ applyFilters(target); });
        });
        document.querySelectorAll('.table-search-clear').forEach(function(btn){
            var target = btn.getAttribute('data-target');
            btn.addEventListener('click', function(){
                var inp = document.querySelector('.table-search[data-target="'+target+'"]');
                if(inp){ inp.value = ''; }
                document.querySelectorAll('.table-filter[data-target="'+target+'"]').forEach(function(f){
                    if(f.tagName.toLowerCase() === 'select') f.selectedIndex = 0; else f.value = '';
                });
                applyFilters(target);
            });
        });
        // initial apply to respect default select state
        document.querySelectorAll('table.admin-table').forEach(function(t){
            var entity = t.getAttribute('data-entity'); if(entity) applyFilters(entity);
        });

        // Sorting
        document.querySelectorAll('table.admin-table').forEach(function(table){
            var thead = table.tHead; if(!thead) return;
            var headers = Array.from(thead.rows[0].cells);
            headers.forEach(function(th, idx){
                var txt = th.textContent.trim().toLowerCase();
                if(txt === 'acciones') return; // skip action column
                th.style.cursor = 'pointer';
                var arrow = document.createElement('span'); arrow.className = 'sort-arrow ms-1'; th.appendChild(arrow);
                th.addEventListener('click', function(){
                    // toggle
                    var cur = th.dataset.sortDir || 'desc';
                    var next = cur === 'asc' ? 'desc' : 'asc';
                    // clear others
                    headers.forEach(function(h){ delete h.dataset.sortDir; var a = h.querySelector('.sort-arrow'); if(a) a.textContent = ''; });
                    th.dataset.sortDir = next;
                    arrow.textContent = next === 'asc' ? ' ▲' : ' ▼';
                    sortTableByColumn(table, idx, next);
                });
            });

            // initialize default: prefer date-like header (containing 'fecha') to be desc
            var dateIdx = headers.map(h=>h.textContent.trim().toLowerCase()).findIndex(t=>t.indexOf('fecha')!==-1);
            if(dateIdx >= 0){
                var dth = headers[dateIdx]; dth.dataset.sortDir = 'desc';
                var a = dth.querySelector('.sort-arrow'); if(a) a.textContent = ' ▼';
                sortTableByColumn(table, dateIdx, 'desc');
            }
        });
    });
})();
