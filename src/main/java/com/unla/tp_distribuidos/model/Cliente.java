package com.unla.tp_distribuidos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Representa a un cliente que puede alquilar vehículos.
 * Guarda la información personal y el estado activo/inactivo del cliente.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long documento;

    private String nombre;
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private Boolean activo = true;

    public Cliente() {}

    // Métodos de dominio
    /**
     * Da de baja al cliente para que no pueda alquilar más vehículos.
     */
    public void darDeBaja() {
        this.activo = false;
    }

    /**
     * Verifica si el cliente está habilitado para alquilar.
     * @return true si está activo, false en caso contrario.
     */
    public boolean puedeAlquilar() {
        return Boolean.TRUE.equals(this.activo);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumento() { return documento; }
    public void setDocumento(Long documento) { this.documento = documento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}