package com.uex.fablab.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Booking;
import com.uex.fablab.model.Shift;
import com.uex.fablab.model.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByUserAndShift(User user, Shift shift);
}
