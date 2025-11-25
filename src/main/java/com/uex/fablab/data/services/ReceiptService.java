package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.repository.ReceiptRepository;

@Service
@Transactional
public class ReceiptService {
    private final ReceiptRepository repo;
    public ReceiptService(ReceiptRepository repo) { this.repo = repo; }
    public List<Receipt> listAll() { return repo.findAll(); }
    public Optional<Receipt> findById(Long id) { return repo.findById(id); }
    public Receipt save(Receipt r) { return repo.save(r); }
    public boolean delete(Long id) { if (!repo.existsById(id)) return false; repo.deleteById(id); return true; }
}
