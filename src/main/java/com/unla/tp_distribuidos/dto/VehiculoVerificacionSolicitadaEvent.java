package com.unla.tp_distribuidos.dto;

import java.io.Serializable;

public class VehiculoVerificacionSolicitadaEvent implements Serializable {
    private Long vehiculoId;
    private String patente;
    private String origen; // "FLOTA_PROPIA" o "INCORPORACION_EXTERNA"

    public VehiculoVerificacionSolicitadaEvent() {}

    public VehiculoVerificacionSolicitadaEvent(Long vehiculoId, String patente, String origen) {
        this.vehiculoId = vehiculoId;
        this.patente = patente;
        this.origen = origen;
    }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}