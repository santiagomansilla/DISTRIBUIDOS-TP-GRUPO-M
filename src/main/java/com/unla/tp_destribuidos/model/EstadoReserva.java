package com.unla.tp_distribuidos.model;

/**
 * Representa los estados que puede tener una reserva.
 *
 * CONFIRMADA: la reserva fue aceptada y está vigente.
 * CANCELADA: la reserva fue anulada antes de completarse.
 * FINALIZADA: la reserva ya fue cumplida o cerrada.
 */
public enum EstadoReserva {
    CONFIRMADA, CANCELADA, FINALIZADA
}