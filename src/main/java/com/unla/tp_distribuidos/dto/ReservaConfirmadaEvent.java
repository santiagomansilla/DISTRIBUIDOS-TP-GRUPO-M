package com.unla.tp_distribuidos.dto;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ReservaConfirmadaEvent implements Serializable {
    private long reservaId;
    private long vehiculoId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;


    public ReservaConfirmadaEvent() {
	}

	public ReservaConfirmadaEvent(long reservaId, long vehiculoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		this.reservaId = reservaId;
		this.vehiculoId = vehiculoId;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
	}
	public long getReservaId() {
		return reservaId;
	}
	public void setReservaId(long reservaId) {
		this.reservaId = reservaId;
	}
	public long getVehiculoId() {
		return vehiculoId;
	}
	public void setVehiculoId(long vehiculoId) {
		this.vehiculoId = vehiculoId;
	}
	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}
	public void setFechaInicio(LocalDateTime fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
	public LocalDateTime getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(LocalDateTime fechaFin) {
		this.fechaFin = fechaFin;
	}
}
