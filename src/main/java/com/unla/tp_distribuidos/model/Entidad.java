package com.unla.tp_distribuidos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Representa una reserva de alquiler de un vehículo.
 * Relaciona un cliente con un vehículo, la fecha del alquiler y el estado de la reserva.
 */
@Entity
@Table(name = "reservas")
public class Entidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private BigDecimal precioDiario;
    private BigDecimal importeTotal;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    public Entidad() {}

    // Métodos de dominio
    /**
     * Calcula el importe total de la reserva según la cantidad de días y el precio diario.
     */
    public void calcularImporteTotal() {
        if (fechaHoraInicio != null && fechaHoraFin != null && precioDiario != null) {
            long dias = Duration.between(fechaHoraInicio, fechaHoraFin).toDays();
            if (dias < 1) dias = 1;
            this.importeTotal = precioDiario.multiply(BigDecimal.valueOf(dias));
        }
    }

    /**
     * Cancela la reserva y la marca como cancelada.
     */
    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }

    public BigDecimal getPrecioDiario() { return precioDiario; }
    public void setPrecioDiario(BigDecimal precioDiario) { this.precioDiario = precioDiario; }

    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal importeTotal) { this.importeTotal = importeTotal; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
}