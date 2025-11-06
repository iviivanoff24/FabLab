package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
    //User findByEmailAndPassword(String email, String password);
}
