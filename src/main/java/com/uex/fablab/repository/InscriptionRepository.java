package com.uex.fablab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Inscription;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
}
