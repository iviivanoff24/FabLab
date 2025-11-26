package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Machine;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.Machine}.
 */
public interface MachineRepository extends JpaRepository<Machine, Long> {
}
