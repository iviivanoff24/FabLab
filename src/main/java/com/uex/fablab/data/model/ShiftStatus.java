package com.uex.fablab.data.model;

/**
 * Estado de un {@link com.uex.fablab.data.model.Shift} (turno).
 * Los valores se utilizan para mapear el estado en la base de datos.
 */
public enum ShiftStatus {
    Disponible,
    Reservado,
    Cancelado
}
