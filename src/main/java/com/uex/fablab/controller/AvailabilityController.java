package com.uex.fablab.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uex.fablab.data.model.Machine;
import com.uex.fablab.data.model.Shift;
import com.uex.fablab.data.model.ShiftStatus;
import com.uex.fablab.data.services.BookingService;
import com.uex.fablab.data.services.MachineService;
import com.uex.fablab.data.services.ShiftService;

/**
 * Endpoint REST para exponer ocupación semanal por franjas horarias.
 */
@RestController
public class AvailabilityController {

    private final MachineService machineService;
    private final ShiftService shiftService;
    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(AvailabilityController.class);

    public AvailabilityController(MachineService machineService, ShiftService shiftService, BookingService bookingService) {
        this.machineService = machineService;
        this.shiftService = shiftService;
        this.bookingService = bookingService;
    }

    /**
     * Devuelve un mapa con claves "YYYY-MM-DD|HH:MM" y valores { occupied, total }.
     * Parámetro `start` esperado en formato ISO (YYYY-MM-DD) que indica el lunes de la semana.
     */
    @GetMapping("/api/availability/week")
    public Map<String, Object> weekAvailability(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start) {
        Map<String, Object> out = new HashMap<>();
        Map<String, Map<String, Integer>> slots = new HashMap<>();

        // Definir franjas (coincide con las usadas en la UI: 09:00..20:00).
        int fromHour = 9;
        int toHour = 20;

        // Contar máquinas consideradas en total (solo las que estén en estado Disponible)
        List<Machine> machines = machineService.listAll();
        Set<Long> availableMachineIds = new HashSet<>();
        int totalMachines = 0;
        for (Machine m : machines) {
            if (m.getStatus() != null && "Disponible".equalsIgnoreCase(m.getStatus().name())) {
                totalMachines++;
                if (m.getId() != null) availableMachineIds.add(m.getId());
            }
        }
        // Fallback: si no hay máquinas marcadas como Disponible, usar todas
        if (totalMachines == 0) {
            totalMachines = machines.size();
            for (Machine m : machines) if (m.getId() != null) availableMachineIds.add(m.getId());
        }

        // Inicializar slots con 0 ocupadas
        for (int d = 0; d < 7; d++) {
            LocalDate date = start.plusDays(d);
            for (int h = fromHour; h <= toHour; h++) {
                String key = date.toString() + "|" + (h < 10 ? "0" + h : h) + ":00";
                Map<String, Integer> info = new HashMap<>();
                info.put("occupied", 0);
                info.put("total", totalMachines);
                slots.put(key, info);
            }
        }

        // Obtener todos los turnos de la semana en una sola consulta
        LocalDate weekEnd = start.plusDays(6);
        List<Shift> weekShifts = shiftService.findByDateBetween(start, weekEnd);

        // Obtener en lote las reservas asociadas a esos turnos para no hacer existsByShift por cada uno
        List<com.uex.fablab.data.model.Booking> bookings = bookingService.findByShifts(weekShifts);
        Set<Long> shiftsWithBookings = new HashSet<>();
        for (com.uex.fablab.data.model.Booking b : bookings) {
            if (b.getShift() != null && b.getShift().getId() != null) shiftsWithBookings.add(b.getShift().getId());
        }

        // Contar ocupación por clave (fecha|hora) recorriendo los turnos
        for (Shift s : weekShifts) {
            if (s.getMachine() == null || s.getMachine().getId() == null) continue;
            if (!availableMachineIds.contains(s.getMachine().getId())) continue; // máquina fuera del pool
            LocalDate date = s.getDate();
            LocalTime st = s.getStartTime();
            if (date == null || st == null) continue;
            int h = st.getHour();
            if (h < fromHour || h > toHour) continue;
            String key = date.toString() + "|" + (h < 10 ? "0" + h : h) + ":00";
            boolean occupied = shiftsWithBookings.contains(s.getId()) || s.getStatus() == ShiftStatus.Reservado;
            if (occupied) {
                Map<String, Integer> info = slots.get(key);
                if (info != null) info.put("occupied", info.getOrDefault("occupied", 0) + 1);
            }
        }

        out.put("slots", slots);
        out.put("totalMachines", totalMachines);
        return out;
    }
}
