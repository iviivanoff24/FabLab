package com.uex.fablab.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Machine;
import com.uex.fablab.model.Shift;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByMachineAndDate(Machine machine, LocalDate date);
}
