package com.uex.fablab.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uex.fablab.model.Booking;
import com.uex.fablab.model.Machine;
import com.uex.fablab.model.Shift;
import com.uex.fablab.model.ShiftStatus;
import com.uex.fablab.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryCrudTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    private Booking newBooking(User u, Shift s) {
        Booking b = new Booking();
        b.setUser(u);
        b.setShift(s);
        b.setFechaReserva(LocalDate.now());
        b.setEstado("Confirmada");
        return b;
    }

    @Test
    @DisplayName("Find booking by user and shift")
    void findByUserAndShift() {
        User u = new User();
        u.setName("U2");
        u.setEmail("u2" + System.nanoTime() + "@example.com");
        u.setPassword("p");
        u.setAdmin(false);
        u = userRepository.save(u);

        Machine m = new Machine();
        m.setName("M2");
        m.setDescription("d");
        m.setLocation("lab");
        m.setStatus(com.uex.fablab.model.MachineStatus.Disponible);
        m = machineRepository.save(m);

        Shift s = new Shift();
        s.setMachine(m);
        s.setDate(LocalDate.now());
        s.setStartTime(LocalTime.of(10, 0));
        s.setEndTime(LocalTime.of(11, 0));
        s.setStatus(ShiftStatus.Disponible);
        s = shiftRepository.save(s);

        Booking saved = bookingRepository.save(newBooking(u, s));
        Optional<Booking> found = bookingRepository.findByUserAndShift(u, s);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }
}
