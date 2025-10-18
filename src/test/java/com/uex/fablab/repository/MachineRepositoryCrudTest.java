package com.uex.fablab.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.Machine;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MachineRepositoryCrudTest {

    @Autowired
    private MachineRepository machineRepository;

    private Machine newMachine(String name, String desc, int stock) {
        Machine m = new Machine();
        m.setName(name);
        m.setDescription(desc);
        m.setStock(stock);
        return m;
    }

    @Test
    @DisplayName("Create and Read Machine")
    void createAndReadMachine() {
        Machine saved = machineRepository.save(newMachine("Prusa MK3S+", "Impresora FDM", 2));
        assertThat(saved.getId()).isNotNull();

        Optional<Machine> found = machineRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Prusa MK3S+");
        assertThat(found.get().getStock()).isEqualTo(2);
    }

    @Test
    @DisplayName("Update Machine fields")
    void updateMachine() {
        Machine saved = machineRepository.save(newMachine("Elegoo Mars", "Impresora resina", 1));
        saved.setStock(3);
        saved.setDescription("Impresora resina LCD");
        Machine updated = machineRepository.save(saved);

        assertThat(updated.getStock()).isEqualTo(3);
        assertThat(updated.getDescription()).isEqualTo("Impresora resina LCD");
    }

    @Test
    @DisplayName("Delete Machine")
    void deleteMachine() {
        Machine saved = machineRepository.save(newMachine("Cortadora Láser", "CO2 60W", 1));
        Long id = saved.getId();
        machineRepository.deleteById(id);
        assertThat(machineRepository.findById(id)).isEmpty();
    }
}
