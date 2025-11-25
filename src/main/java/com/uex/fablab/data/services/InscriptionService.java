package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Inscription;
import com.uex.fablab.data.repository.InscriptionRepository;

/**
 * Servicio de inscripciones a cursos.
 */
@Service
@Transactional
public class InscriptionService {
    private final InscriptionRepository repo;
    public InscriptionService(InscriptionRepository repo) { this.repo = repo; }
    /** Lista todas las inscripciones. */
    public List<Inscription> listAll() { return repo.findAll(); }
    /** Busca inscripción por id. */
    public Optional<Inscription> findById(Long id) { return repo.findById(id); }
    /** Guarda inscripción. */
    public Inscription save(Inscription i) { return repo.save(i); }
    /** Elimina inscripción por id. */
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
