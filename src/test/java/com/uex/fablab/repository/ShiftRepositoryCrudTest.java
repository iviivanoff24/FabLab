package com.uex.fablab.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    private Shift newShift(Machine m, LocalDate date) {
        Shift s = new Shift();
        s.setMachine(m);
        s.setDate(date);
        s.setStartTime(LocalTime.of(9, 0));
        s.setEndTime(LocalTime.of(10, 0));
        s.setStatus(ShiftStatus.Disponible);
        return s;
    }

    @SuppressWarnings("unused")
    @Test
    @DisplayName("Find shifts by machine and date")
    void findByMachineAndDate() {
        Machine m = new Machine();
        m.setName("M1");
        m.setDescription("d");
        m.setLocation("lab");
        m.setStatus(com.uex.fablab.model.MachineStatus.Disponible);
        m = machineRepository.save(m);

        Shift s1 = shiftRepository.save(newShift(m, LocalDate.now()));

        List<Shift> list = shiftRepository.findByMachineAndDate(m, LocalDate.now());
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getMachine().getId()).isEqualTo(m.getId());
    }
}
