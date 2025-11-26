package com.uex.fablab.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.User;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.Booking}.
 * Provee métodos derivados para buscar por usuario y turno.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Busca una reserva por usuario y turno. */
    Optional<Booking> findByUserAndShift(User user, Shift shift);

    /** Lista todas las reservas de un usuario (uso en vistas y tests). */
    List<Booking> findByUser(User user);

    /** Comprueba si existe alguna reserva para un turno. */
    boolean existsByShift(Shift shift);
}
