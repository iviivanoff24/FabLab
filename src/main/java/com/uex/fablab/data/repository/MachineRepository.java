package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.MachineStatus;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.Machine}.
 */
public interface MachineRepository extends JpaRepository<Machine, Long> {
	/** Busca máquinas cuyo nombre contiene el texto (case-insensitive). */
	java.util.List<Machine> findByNameContainingIgnoreCase(String name);

    /** Busca máquinas por estado. */
    java.util.List<Machine> findByStatus(MachineStatus status);
}
