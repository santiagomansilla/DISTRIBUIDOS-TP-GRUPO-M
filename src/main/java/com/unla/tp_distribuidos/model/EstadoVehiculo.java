package com.unla.tp_distribuidos.model;

/**
 * Representa el estado operativo de un vehículo.
 *
 * DISPONIBLE: está listo para alquilarse.
 * ALQUILADO: actualmente está siendo usado por un cliente.
 * MANTENIMIENTO: no puede ser alquilado porque necesita revisión o reparación.
 */
public enum EstadoVehiculo {

    DISPONIBLE,RESERVADO,EN_ALQUILER

}