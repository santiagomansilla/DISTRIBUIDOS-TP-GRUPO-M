package com.unla.tp_distribuidos.model;
/**
 * Representa los estados que puede tener una reserva.
 *
 * EN_PROCESO: SE LLEVA A CABO.
 * REALIZADO: SE IMPLEMENTO EL MANTENIMIENTO DEL VEHICULO.
 * NO_REALIZADA: NO SE REALIZO MANTENIMIENTO.
 */
public enum EstadoMantenimiento {
    REALIZADO, NO_REALIZADO, EN_PROCESO
}