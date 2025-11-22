package com.uex.fablab.usecases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.BookingStatus;
import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.model.CourseStatus;
import com.uex.fablab.data.model.Inscription;
import com.uex.fablab.data.model.InscriptionStatus;
import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.MachineStatus;
import com.uex.fablab.data.model.PaymentMethod;
import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.BookingRepository;
import com.uex.fablab.data.repository.CourseRepository;
import com.uex.fablab.data.repository.InscriptionRepository;
import com.uex.fablab.data.repository.MachineRepository;
import com.uex.fablab.data.repository.ReceiptRepository;
import com.uex.fablab.data.repository.ShiftRepository;
import com.uex.fablab.data.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FablabUseCasesTest {

    @Autowired private UserRepository userRepository;
    @Autowired private MachineRepository machineRepository;
    @Autowired private ShiftRepository shiftRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private InscriptionRepository inscriptionRepository;
    @Autowired private ReceiptRepository receiptRepository;
    
    // Helpers
    private User newUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("secret");
        u.setRole("USER");
        return u;
    }

    private Machine newMachine(String name) {
        Machine m = new Machine();
        m.setName(name);
        m.setDescription("Desc " + name);
        m.setLocation("Sala 1");
        m.setStatus(MachineStatus.Disponible);
        m.setHourlyPrice(new BigDecimal("20.00"));
        return m;
    }

    private Shift newShift(Machine m, LocalDate date, LocalTime start, LocalTime end) {
        Shift s = new Shift();
        s.setMachine(m);
        s.setDate(date);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setStatus(ShiftStatus.Disponible);
        return s;
    }

    private Course newCourse(String name, LocalDate start, LocalDate end) {
        Course c = new Course();
        c.setName(name);
        c.setDescription("Curso " + name);
        c.setCapacity(10);
        c.setStartDate(start);
        c.setEndDate(end);
        c.setPrecio(25.0);
        c.setEstado(CourseStatus.Activo);
        return c;
    }

    // --- USUARIO ---
    @Test
    @Order(1)
    @DisplayName("Usuario: crear y buscar por email")
    void usuarioCrearYBuscar() {
        User saved = userRepository.save(newUser("Ana", "ana@example.com"));
        assertThat(saved.getId()).isNotNull();

        User found = userRepository.findByEmail("ana@example.com");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Ana");
        assertThat(found.isAdmin()).isFalse();
    }

    @Test
    @Order(2)
    @DisplayName("Usuario: email único (constraint)")
    void usuarioEmailUnico() {
        userRepository.save(newUser("Bob", "bob@example.com"));
        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(newUser("Bobby", "bob@example.com"));
        });
        assertThat(ex).isNotNull();
    }

    // --- MAQUINA y TURNOS ---
    @Test
    @Order(3)
    @DisplayName("Maquina y Turnos: crear y consultar por máquina+fecha")
    void turnosCrearYConsultar() {
        Machine m = machineRepository.save(newMachine("Impresora 3D"));
        LocalDate fecha = LocalDate.now().plusDays(1);
        shiftRepository.save(newShift(m, fecha, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        shiftRepository.save(newShift(m, fecha, LocalTime.of(11, 0), LocalTime.of(12, 0)));

        List<Shift> turnos = shiftRepository.findByMachineAndDate(m, fecha);
        assertThat(turnos).hasSize(2);
        assertThat(turnos.get(0).getMachine().getHourlyPrice()).isEqualByComparingTo("20.00");
    }

    // --- RESERVAS ---
    @Test
    @Order(4)
    @DisplayName("Reserva: crear y encontrar por usuario+turno")
    @Transactional
    void reservaCrearYEncontrar() {
        User u = userRepository.save(newUser("Carla", "carla@example.com"));
        Machine m = machineRepository.save(newMachine("Laser Cutter"));
        Shift s = shiftRepository.save(newShift(m, LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(10, 0)));

        Booking b = new Booking();
        b.setUser(u);
        b.setShift(s);
        b.setFechaReserva(LocalDate.now());
        b.setEstado(BookingStatus.Pendiente);
        bookingRepository.save(b);

        Booking found = bookingRepository.findByUserAndShift(u, s).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getEstado()).isEqualTo(BookingStatus.Pendiente);
        assertThat(found.getShift().getMachine().getHourlyPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    @Order(5)
    @DisplayName("Reserva: un usuario puede tener varias reservas")
    @Transactional
    void reservasMultiplesPorUsuario() {
        User u = userRepository.save(newUser("Elena", "elena@example.com"));
        Machine m = machineRepository.save(newMachine("Fresadora"));
        LocalDate fecha = LocalDate.now().plusDays(3);

        Shift s1 = shiftRepository.save(newShift(m, fecha, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        Shift s2 = shiftRepository.save(newShift(m, fecha, LocalTime.of(11, 0), LocalTime.of(12, 0)));

        Booking b1 = new Booking();
        b1.setUser(u);
        b1.setShift(s1);
        b1.setFechaReserva(LocalDate.now());
        b1.setEstado(BookingStatus.Confirmada);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setUser(u);
        b2.setShift(s2);
        b2.setFechaReserva(LocalDate.now());
        b2.setEstado(BookingStatus.Pendiente);
        bookingRepository.save(b2);

        List<Booking> reservas = bookingRepository.findByUser(u);
        assertThat(reservas).hasSize(2);
    }

    @Test
    @Order(6)
    @DisplayName("Reserva: al eliminar un usuario, se eliminan sus reservas (cascade DB)")
    @Transactional
    void eliminarUsuarioEliminaReservas() {
        User u = userRepository.save(newUser("Gonzalo", "gonzalo@example.com"));
        Machine m = machineRepository.save(newMachine("Cortadora"));
        Shift s = shiftRepository.save(newShift(m, LocalDate.now().plusDays(4), LocalTime.of(9, 0), LocalTime.of(10, 0)));

        Booking b = new Booking();
        b.setUser(u);
        b.setShift(s);
        b.setFechaReserva(LocalDate.now());
        b.setEstado(BookingStatus.Pendiente);
        b = bookingRepository.save(b);
        u.getBookings().add(b);
        Long bookingId = b.getId();

        userRepository.delete(u);
        assertThat(bookingRepository.findById(bookingId)).isEmpty();
    }

    // --- CURSOS e INSCRIPCIONES ---
    @Test
    @Order(7)
    @DisplayName("Curso e Inscripción: crear y vincular usuario-curso")
    void cursoInscripcion() {
        User u = userRepository.save(newUser("Diego", "diego@example.com"));
        Course c = courseRepository.save(newCourse("Introducción Fablab", LocalDate.now(), LocalDate.now().plusDays(5)));

        Inscription ins = new Inscription();
        ins.setUser(u);
        ins.setCourse(c);
        ins.setDate(LocalDate.now());
        ins.setEstado(InscriptionStatus.Activo);
        Inscription saved = inscriptionRepository.save(ins);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCourse().getName()).contains("Fablab");
        assertThat(saved.getUser().getEmail()).isEqualTo("diego@example.com");
    }

    // --- RECIBOS ---
    @Test
    @Order(8)
    @DisplayName("Recibo: crear con estados y método de pago")
    void reciboCrear() {
        User u = userRepository.save(newUser("Eva", "eva@example.com"));
        Receipt r = new Receipt();
        r.setUser(u);
        r.setTotalPrice(49.90);
        r.setFechaEmision(LocalDate.now());
        r.setMetodoPago(PaymentMethod.Tarjeta);
        r.setConcepto("Pago curso básico");
        r.setEstadoRecibo(ReceiptStatus.Pendiente);
        Receipt saved = receiptRepository.save(r);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMetodoPago()).isEqualTo(PaymentMethod.Tarjeta);
        assertThat(saved.getEstadoRecibo()).isEqualTo(ReceiptStatus.Pendiente);
    }

    @Test
    @Order(9)
    @DisplayName("Recibo: actualizar estado a Pagado")
    void reciboActualizarEstado() {
        User u = userRepository.save(newUser("Fran", "fran@example.com"));
        Receipt r = new Receipt();
        r.setUser(u);
        r.setTotalPrice(10.00);
        r.setFechaEmision(LocalDate.now());
        r.setMetodoPago(PaymentMethod.Efectivo);
        r.setEstadoRecibo(ReceiptStatus.Pendiente);
        r = receiptRepository.save(r);

        r.setEstadoRecibo(ReceiptStatus.Pagado);
        Receipt updated = receiptRepository.save(r);
        assertThat(updated.getEstadoRecibo()).isEqualTo(ReceiptStatus.Pagado);
    }

    // --- CASCADAS ADICIONALES Y CASOS NEGATIVOS ---
    @Test
    @Order(10)
    @DisplayName("Cascada: eliminar Máquina borra sus Turnos")
    @Transactional
    void eliminarMaquinaBorraTurnos() {
        Machine m = machineRepository.save(newMachine("Plotter"));
        LocalDate fecha = LocalDate.now().plusDays(7);
        Shift s1 = shiftRepository.save(newShift(m, fecha, LocalTime.of(8, 0), LocalTime.of(9, 0)));
        Shift s2 = shiftRepository.save(newShift(m, fecha, LocalTime.of(9, 0), LocalTime.of(10, 0)));

        Long s1Id = s1.getId();
        Long s2Id = s2.getId();

        machineRepository.delete(m);

        assertThat(shiftRepository.findById(s1Id)).isEmpty();
        assertThat(shiftRepository.findById(s2Id)).isEmpty();
        assertThat(shiftRepository.findByMachineAndDate(m, fecha)).isEmpty();
    }

    @Test
    @Order(11)
    @DisplayName("Cascada: eliminar Turno borra sus Reservas")
    @Transactional
    void eliminarTurnoBorraReservas() {
        User u = userRepository.save(newUser("Hugo", "hugo@example.com"));
        Machine m = machineRepository.save(newMachine("Router CNC"));
        Shift s = shiftRepository.save(newShift(m, LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(15, 0)));

        Booking b = new Booking();
        b.setUser(u);
        b.setShift(s);
        b.setFechaReserva(LocalDate.now());
        b.setEstado(BookingStatus.Pendiente);
        b = bookingRepository.save(b);
        Long bId = b.getId();

        shiftRepository.delete(s);
        assertThat(bookingRepository.findById(bId)).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Cascada: eliminar Curso borra sus Inscripciones")
    @Transactional
    void eliminarCursoBorraInscripciones() {
        User u = userRepository.save(newUser("Irene", "irene@example.com"));
        Course c = courseRepository.save(newCourse("Avanzado Fablab", LocalDate.now(), LocalDate.now().plusDays(3)));
        Inscription ins = new Inscription();
        ins.setUser(u);
        ins.setCourse(c);
        ins.setDate(LocalDate.now());
        ins.setEstado(InscriptionStatus.Activo);
        ins = inscriptionRepository.save(ins);
        Long insId = ins.getId();

        courseRepository.delete(c);
        assertThat(inscriptionRepository.findById(insId)).isEmpty();
    }

    @Test
    @Order(13)
    @DisplayName("Cascada: eliminar Usuario borra Inscripciones y Recibos")
    @Transactional
    void eliminarUsuarioBorraInscripcionesYRecibos() {
        User u = userRepository.save(newUser("Julia", "julia@example.com"));
        Course c = courseRepository.save(newCourse("Seguridad", LocalDate.now(), LocalDate.now().plusDays(1)));
        Inscription ins = new Inscription();
        ins.setUser(u);
        ins.setCourse(c);
        ins.setDate(LocalDate.now());
        ins.setEstado(InscriptionStatus.Activo);
        ins = inscriptionRepository.save(ins);
        Long insId = ins.getId();

        Receipt r = new Receipt();
        r.setUser(u);
        r.setTotalPrice(5.00);
        r.setMetodoPago(PaymentMethod.Online);
        r.setEstadoRecibo(ReceiptStatus.Pendiente);
        r = receiptRepository.save(r);
        u.getInscriptions().add(ins);
        u.getReceipts().add(r);
        Long rId = r.getId();

        userRepository.delete(u);
        assertThat(inscriptionRepository.findById(insId)).isEmpty();
        assertThat(receiptRepository.findById(rId)).isEmpty();
    }

    @Test
    @Order(14)
    @DisplayName("Consultas: negativos (sin resultados)")
    void consultasNegativas() {
        Machine m = machineRepository.save(newMachine("Cortadora Vinilo"));
        List<Shift> vacio = shiftRepository.findByMachineAndDate(m, LocalDate.now().plusYears(1));
        assertThat(vacio).isEmpty();

        User u = userRepository.save(newUser("Kevin", "kevin@example.com"));
        Shift s = shiftRepository.save(newShift(m, LocalDate.now().plusDays(10), LocalTime.of(16, 0), LocalTime.of(17, 0)));
        assertThat(bookingRepository.findByUserAndShift(u, s)).isEmpty();
    }

    @Test
    @Order(15)
    @DisplayName("Recibo: fecha por defecto se establece si no se especifica")
    void reciboFechaPorDefecto() {
        User u = userRepository.save(newUser("Laura", "laura@example.com"));
        Receipt r = new Receipt();
        r.setUser(u);
        r.setTotalPrice(15.00);
        r.setMetodoPago(PaymentMethod.Efectivo);
        r.setEstadoRecibo(ReceiptStatus.Pendiente);

        Receipt saved = receiptRepository.save(r);
        assertThat(saved.getFechaEmision()).isNotNull();
    }
}
