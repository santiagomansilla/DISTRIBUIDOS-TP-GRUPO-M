package com.unla.tp_distribuidos.dto;

import java.io.Serializable;

public class TitularidadResultadoDTO implements Serializable {
    private Long vehiculoId;
    private String patente;
    private boolean esTitular;
    private String motivo;

    public TitularidadResultadoDTO() {}

    public TitularidadResultadoDTO(Long vehiculoId, String patente, boolean esTitular, String motivo) {
        this.vehiculoId = vehiculoId;
        this.patente = patente;
        this.esTitular = esTitular;
        this.motivo = motivo;
    }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public boolean isEsTitular() { return esTitular; }
    public void setEsTitular(boolean esTitular) { this.esTitular = esTitular; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}