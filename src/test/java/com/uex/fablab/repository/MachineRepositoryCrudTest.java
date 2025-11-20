package com.uex.fablab.repository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.MachineStatus;
import com.uex.fablab.data.repository.MachineRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MachineRepositoryCrudTest {

    @Autowired
    private MachineRepository machineRepository;

    private Machine newMachine(String name) {
        Machine m = new Machine();
        m.setName(name);
        m.setDescription("desc");
        m.setLocation("lab");
        m.setStatus(MachineStatus.Disponible);
        m.setHourlyPrice(new BigDecimal("12.50"));
        return m;
    }

    @Test
    @DisplayName("Create and Read Machine")
    void createAndReadMachine() {
        Machine saved = machineRepository.save(newMachine("Laser"));
        assertThat(saved.getId()).isNotNull();

        Machine found = machineRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Laser");
        assertThat(found.getHourlyPrice()).isEqualByComparingTo("12.50");
    }
}
