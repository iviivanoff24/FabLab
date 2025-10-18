package com.uex.fablab.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.Machine;
import com.uex.fablab.model.Shift;
import com.uex.fablab.model.ShiftStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShiftRepositoryCrudTest {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private MachineRepository machineRepository;

    private Machine newMachine(String name) {
        Machine m = new Machine();
        m.setName(name);
        m.setStock(1);
        return m;
    }

    private Shift newShift(Machine machine, LocalDate date, String s, String e) {
        Shift sh = new Shift();
        sh.setMachine(machine);
        sh.setDate(date);
        sh.setStartTime(LocalTime.parse(s));
        sh.setEndTime(LocalTime.parse(e));
        sh.setStatus(ShiftStatus.available);
        return sh;
    }

    @Test
    @DisplayName("Create and Read Shift")
    void createAndReadShift() {
        Machine m = machineRepository.save(newMachine("Impresora FDM"));
        Shift saved = shiftRepository.save(newShift(m, LocalDate.now().plusDays(1), "10:00", "12:00"));
        assertThat(saved.getId()).isNotNull();

        Optional<Shift> found = shiftRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMachine().getId()).isEqualTo(m.getId());
        assertThat(found.get().getStatus()).isEqualTo(ShiftStatus.available);
    }

    @Test
    @DisplayName("Update Shift fields")
    void updateShift() {
        Machine m = machineRepository.save(newMachine("CNC"));
        Shift saved = shiftRepository.save(newShift(m, LocalDate.now().plusDays(2), "09:00", "11:00"));
        saved.setStatus(ShiftStatus.maintenance);
        saved.setEndTime(LocalTime.parse("12:00"));
        Shift updated = shiftRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(ShiftStatus.maintenance);
        assertThat(updated.getEndTime()).isEqualTo(LocalTime.parse("12:00"));
    }

    @Test
    @DisplayName("Delete Shift")
    void deleteShift() {
        Machine m = machineRepository.save(newMachine("Láser"));
        Shift saved = shiftRepository.save(newShift(m, LocalDate.now().plusDays(3), "15:00", "17:00"));
        Long id = saved.getId();
        shiftRepository.deleteById(id);
        assertThat(shiftRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("findByMachineAndDate returns shifts")
    void findByMachineAndDate() {
        Machine m = machineRepository.save(newMachine("Fresadora"));
        LocalDate d = LocalDate.now().plusDays(4);
        shiftRepository.save(newShift(m, d, "08:00", "10:00"));
        shiftRepository.save(newShift(m, d, "10:00", "12:00"));

        List<Shift> shifts = shiftRepository.findByMachineAndDate(m, d);
        assertThat(shifts).hasSize(2);
    }
}
