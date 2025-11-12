package com.uex.fablab.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByUserAndShift(User user, Shift shift);

    // Listar todas las reservas de un usuario (soporte para tests y casos de uso)
    List<Booking> findByUser(User user);
}
