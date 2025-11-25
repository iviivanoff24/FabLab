package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.repository.MachineRepository;

@Service
@Transactional
public class MachineService {
    private final MachineRepository repo;

    public MachineService(MachineRepository repo) {
        this.repo = repo;
    }

    public List<Machine> listAll() {
        return repo.findAll();
    }

    public Optional<Machine> findById(Long id) {
        return repo.findById(id);
    }

    public Machine save(Machine m) {
        return repo.save(m);
    }

    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
