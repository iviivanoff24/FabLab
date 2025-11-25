package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Booking;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.BookingRepository;

@Service
@Transactional
public class BookingService {
    private final BookingRepository repo;

    public BookingService(BookingRepository repo) {
        this.repo = repo;
    }

    public List<Booking> listAll() { return repo.findAll(); }
    public Optional<Booking> findById(Long id) { return repo.findById(id); }
    public Optional<Booking> findByUserAndShift(User u, Shift s) { return repo.findByUserAndShift(u, s); }
    public List<Booking> findByUser(User u) { return repo.findByUser(u); }
    public Booking save(Booking b) { return repo.save(b); }
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
