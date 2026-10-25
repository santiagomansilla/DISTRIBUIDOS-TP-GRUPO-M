package com.unla.tp_distribuidos.dto;

import java.io.Serializable;

public class VehiculoVerificacionResultadoEvent implements Serializable {
    private Long vehiculoId;
    private String patente;
    private String resultado; // "VALIDADO" o "ROBADO"
    private String origen;

    public VehiculoVerificacionResultadoEvent() {}

    public VehiculoVerificacionResultadoEvent(Long vehiculoId, String patente, String resultado, String origen) {
        this.vehiculoId = vehiculoId;
        this.patente = patente;
        this.resultado = resultado;
        this.origen=origen;
    }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}