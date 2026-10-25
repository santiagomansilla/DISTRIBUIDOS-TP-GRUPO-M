package com.unla.tp_distribuidos.dto;
import java.io.Serializable;

import com.unla.tp_distribuidos.model.EstadoMantenimiento;


public class MantenimientoResultadoEvent implements Serializable{

private Long reservaId;
private Long vehiculoId;
private EstadoMantenimiento resultado;
public Long getReservaId() {
	return reservaId;
}
public void setReservaId(Long reservaId) {
	this.reservaId = reservaId;
}
public Long getVehiculoId() {
	return vehiculoId;
}
public void setVehiculoId(Long vehiculoId) {
	this.vehiculoId = vehiculoId;
}
public EstadoMantenimiento getResultado() {
	return resultado;
}
public void setResultado(EstadoMantenimiento resultado) {
	this.resultado = resultado;
}
public MantenimientoResultadoEvent(Long reservaId, Long vehiculoId, EstadoMantenimiento resultado) {
	this.reservaId = reservaId;
	this.vehiculoId = vehiculoId;
	this.resultado = resultado;
}
public MantenimientoResultadoEvent() {
}



}