package com.unla.tp_distribuidos.dto;

import java.io.Serializable;

public class TitularidadSolicitudDTO implements Serializable {
    private Long vehiculoId;
    private String patente;
    private Long documentoCliente;

    public TitularidadSolicitudDTO() {}

    public TitularidadSolicitudDTO(Long vehiculoId, String patente, Long documentoCliente) {
        this.vehiculoId = vehiculoId;
        this.patente = patente;
        this.documentoCliente = documentoCliente;
    }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public Long getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(Long documentoCliente) { this.documentoCliente = documentoCliente; }
}