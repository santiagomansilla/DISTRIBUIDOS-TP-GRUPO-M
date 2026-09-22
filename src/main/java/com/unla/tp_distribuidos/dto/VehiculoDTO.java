package com.unla.tp_distribuidos.dto;

import com.unla.tp_distribuidos.model.EstadoVehiculo;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class VehiculoDTO {

    @NotBlank(message = "La patente es obligatoria")
    private String patente;

    private String marca;
    private String modelo;
    private Integer anio;
    private String color;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipo;

    @NotNull(message = "El precio diario es obligatorio")
    private BigDecimal precioDiario;

    private EstadoVehiculo estado;

    // Getters y Setters
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }

    public BigDecimal getPrecioDiario() { return precioDiario; }
    public void setPrecioDiario(BigDecimal precioDiario) { this.precioDiario = precioDiario; }

    public EstadoVehiculo getEstado() { return estado; }
    public void setEstado(EstadoVehiculo estado) { this.estado = estado; }
}
