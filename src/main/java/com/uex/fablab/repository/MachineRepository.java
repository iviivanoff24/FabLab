package com.uex.fablab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}
