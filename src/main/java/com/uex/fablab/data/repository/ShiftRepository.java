package com.uex.fablab.data.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.Shift;

/**
 * Repositorio para {@link com.uex.fablab.data.model.Shift} (turnos).
 */
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /** Busca turnos por máquina y fecha. */
    List<Shift> findByMachineAndDate(Machine machine, LocalDate date);

    /** Busca turnos dentro de un rango de fechas (inclusive). */
    List<Shift> findByDateBetween(LocalDate start, LocalDate end);
}
