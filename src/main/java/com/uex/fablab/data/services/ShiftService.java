package com.uex.fablab.data.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.repository.ShiftRepository;

/**
 * Servicio de turnos.
 * Gestiona CRUD de {@link com.uex.fablab.data.model.Shift} y búsquedas por máquina y fecha.
 */
@Service
@Transactional
public class ShiftService {
    private final ShiftRepository repo;
    public ShiftService(ShiftRepository repo) { this.repo = repo; }
    /** Lista todos los turnos. */
    public List<Shift> listAll() { return repo.findAll(); }
    /** Busca turno por id. */
    public Optional<Shift> findById(Long id) { return repo.findById(id); }
    /** Lista turnos por máquina y fecha. */
    public List<Shift> findByMachineAndDate(Machine m, LocalDate d) { return repo.findByMachineAndDate(m, d); }
    /** Busca turnos en un rango de fechas. */
    public List<Shift> findByDateBetween(LocalDate start, LocalDate end) { return repo.findByDateBetween(start, end); }
    /** Guarda turno. */
    public Shift save(Shift s) { return repo.save(s); }
    /** Elimina turno por id. */
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
