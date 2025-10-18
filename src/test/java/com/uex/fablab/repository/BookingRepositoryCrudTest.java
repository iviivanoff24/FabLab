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
    private ShiftRepository shiftRepository;
    @Autowired
    private MachineRepository machineRepository;

    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("secret");
        u.setAdmin(false);
        return u;
    }

    private Machine newMachine(String name) {
        Machine m = new Machine();
        m.setName(name);
        m.setStock(1);
        return m;
    }

    private Shift newShift(Machine m, LocalDate d, String s, String e) {
        Shift sh = new Shift();
        sh.setMachine(m);
        sh.setDate(d);
        sh.setStartTime(LocalTime.parse(s));
        sh.setEndTime(LocalTime.parse(e));
        sh.setStatus(ShiftStatus.available);
        return sh;
    }

    @Test
    @DisplayName("Create and Read Booking")
    void createAndReadBooking() {
        User user = userRepository.save(newUser("Eva", "eva@example.com"));
        Machine machine = machineRepository.save(newMachine("Impresora"));
        Shift shift = shiftRepository.save(newShift(machine, LocalDate.now().plusDays(1), "10:00", "12:00"));

        Booking saved = new Booking();
        saved.setUser(user);
        saved.setShift(shift);
        saved = bookingRepository.save(saved);
        assertThat(saved.getId()).isNotNull();

        Optional<Booking> found = bookingRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getShift().getId()).isEqualTo(shift.getId());
    }

    @Test
    @DisplayName("Update Booking")
    void updateBooking() {
        User user = userRepository.save(newUser("Fran", "fran@example.com"));
        Machine machine = machineRepository.save(newMachine("Láser"));
        Shift shift1 = shiftRepository.save(newShift(machine, LocalDate.now().plusDays(2), "09:00", "10:00"));
        Shift shift2 = shiftRepository.save(newShift(machine, LocalDate.now().plusDays(3), "11:00", "12:00"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShift(shift1);
        booking = bookingRepository.save(booking);

        booking.setShift(shift2);
        Booking updated = bookingRepository.save(booking);
        assertThat(updated.getShift().getId()).isEqualTo(shift2.getId());
    }

    @Test
    @DisplayName("Delete Booking")
    void deleteBooking() {
        User user = userRepository.save(newUser("Gus", "gus@example.com"));
        Machine machine = machineRepository.save(newMachine("CNC"));
        Shift shift = shiftRepository.save(newShift(machine, LocalDate.now().plusDays(4), "15:00", "16:00"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShift(shift);
        Booking saved = bookingRepository.save(booking);
        Long id = saved.getId();
        bookingRepository.deleteById(id);
        assertThat(bookingRepository.findById(id)).isEmpty();
    }
}
