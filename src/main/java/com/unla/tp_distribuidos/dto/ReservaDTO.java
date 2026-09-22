package com.unla.tp_distribuidos.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReservaDTO {

    @NotNull(message = "El ID de cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El ID de vehículo es obligatorio")
    private Long vehiculoId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    private LocalDateTime fechaHoraFin;

    // Getters y Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
}