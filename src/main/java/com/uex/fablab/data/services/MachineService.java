package com.uex.fablab.data.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.MachineStatus;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.repository.MachineRepository;
import com.uex.fablab.data.repository.ShiftRepository;

/**
 * Servicio de máquinas.
 * Encapsula acceso a repositorio y operaciones auxiliares para gestionar imágenes.
 */
@Service
@Transactional
public class MachineService {
    private final MachineRepository repo;
    private final ShiftRepository shiftRepo;

    public MachineService(MachineRepository repo, ShiftRepository shiftRepo) {
        this.repo = repo;
        this.shiftRepo = shiftRepo;
    }

    /** Lista todas las máquinas. */
    public List<Machine> listAll() {
        return repo.findAll();
    }

    /** Lista máquinas cuyo nombre contiene el texto dado (insensible a mayúsculas). */
    public List<Machine> searchByName(String text) {
        if (text == null || text.isBlank()) return repo.findAll();
        return repo.findByNameContainingIgnoreCase(text.trim());
    }

    /** Busca máquinas disponibles en una fecha y hora. */
    public List<Machine> findAvailableMachines(LocalDate date, LocalTime time) {
        List<Machine> allMachines = repo.findByStatus(MachineStatus.Disponible);
        List<Shift> reservedShifts = shiftRepo.findByDateAndStartTimeAndStatus(date, time, ShiftStatus.Reservado);
        Set<Long> reservedMachineIds = reservedShifts.stream().map(s -> s.getMachine().getId()).collect(Collectors.toSet());
        return allMachines.stream()
                .filter(m -> !reservedMachineIds.contains(m.getId()))
                .collect(Collectors.toList());
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
