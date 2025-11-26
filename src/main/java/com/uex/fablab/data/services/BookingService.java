package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.BookingRepository;

/**
 * Servicio de reservas.
 * Proporciona operaciones CRUD y utilidades de búsqueda relacionadas con {@link com.uex.fablab.data.model.Booking}.
 */
@Service
@Transactional
public class BookingService {
    private final BookingRepository repo;

    public BookingService(BookingRepository repo) {
        this.repo = repo;
    }

    /** Lista todas las reservas. */
    public List<Booking> listAll() { return repo.findAll(); }
    /** Busca una reserva por id. */
    public Optional<Booking> findById(Long id) { return repo.findById(id); }
    /** Busca una reserva por usuario y turno. */
    public Optional<Booking> findByUserAndShift(User u, Shift s) { return repo.findByUserAndShift(u, s); }
    /** Lista reservas de un usuario. */
    public List<Booking> findByUser(User u) { return repo.findByUser(u); }
    /** Comprueba si existe alguna reserva para un turno. */
    public boolean existsByShift(Shift s) { return repo.existsByShift(s); }
    /** Guarda una reserva. */
    public Booking save(Booking b) { return repo.save(b); }
    /** Elimina una reserva por id. */
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
