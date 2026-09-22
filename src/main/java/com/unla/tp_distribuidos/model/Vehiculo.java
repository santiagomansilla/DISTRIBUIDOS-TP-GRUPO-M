package com.unla.tp_distribuidos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Clase que representa un vehículo del sistema de alquiler.
 *
 * Cada vehículo tiene su identificación, características físicas,
 * tipo de transporte, estado operativo y precio diario de alquiler.
 * También se controla si el vehículo está disponible para ser usado o no.
 */
@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    /**
     * Identificador único del vehículo en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Patente del vehículo. Debe ser única para evitar duplicados.
     */
    @Column(nullable = false, unique = true)
    private String patente;

    /**
     * Marca del vehículo.
     */
    private String marca;

    /**
     * Modelo del vehículo.
     */
    private String modelo;

    /**
     * Año de fabricación del vehículo.
     */
    private Integer anio;

    /**
     * Color visible del vehículo.
     */
    private String color;

    /**
     * Categoría del vehículo: auto, camioneta o moto.
     */
    @Enumerated(EnumType.STRING)
    private TipoVehiculo tipo;

    /**
     * Precio por día de alquiler del vehículo.
     */
    private BigDecimal precioDiario;

    /**
     * Estado actual del vehículo: disponible, alquilado o en mantenimiento.
     */
    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado;

    /**
     * Indica si el vehículo está habilitado para su uso.
     * Los vehículos nuevos comienzan activos por defecto.
     */
    @Column(nullable = false)
    private Boolean activo = true;

    public Vehiculo() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}