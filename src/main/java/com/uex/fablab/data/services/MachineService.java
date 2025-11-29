package com.uex.fablab.data.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.repository.MachineRepository;

/**
 * Servicio de máquinas.
 * Encapsula acceso a repositorio y operaciones auxiliares para gestionar imágenes.
 */
@Service
@Transactional
public class MachineService {
    private final MachineRepository repo;

    public MachineService(MachineRepository repo) {
        this.repo = repo;
    }

    /** Lista todas las máquinas. */
    public List<Machine> listAll() {
        return repo.findAll();
    }

    /** Busca una máquina por id. */
    public Optional<Machine> findById(Long id) {
        return repo.findById(id);
    }

    /** Guarda una máquina. */
    public Machine save(Machine m) {
        return repo.save(m);
    }

    /**
     * Elimina la máquina por id y borra sus imágenes asociadas del sistema de ficheros.
     * @param id identificador
     * @return true si se eliminó, false si no existe
     */
    public boolean delete(Long id) {
        return repo.findById(id).map(m -> {
            // Borrar imágenes asociadas antes de eliminar la entidad
            deleteMachineImages(m.getId());
            repo.delete(m); // cascadas JPA para turnos
            return true;
        }).orElse(false);
    }

    private void deleteMachineImages(Long id) {
        if (id == null) return;
        String[] exts = {".jpg", ".png", ".gif"};
        Path uploadsDir = resolveUploadsDir();
        for (String ext : exts) {
            try {
                Path p = uploadsDir.resolve("machine-" + id + ext);
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        }
    }

    private Path resolveUploadsDir() {
        // Coincide con la lógica del controller: intentar ruta del módulo y fallback local
        Path moduleDir = Path.of("ProyectoMDAI", "src", "main", "resources", "templates", "img", "upload", "machines");
        if (Files.exists(moduleDir)) return moduleDir;
        Path localDir = Path.of("src", "main", "resources", "templates", "img", "upload", "machines");
        return localDir;
    }
}
