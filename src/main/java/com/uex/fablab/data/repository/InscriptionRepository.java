package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Inscription;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.Inscription}.
 */
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
}
