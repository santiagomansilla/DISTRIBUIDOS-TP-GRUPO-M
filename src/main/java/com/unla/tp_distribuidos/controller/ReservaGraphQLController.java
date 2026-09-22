package com.unla.tp_distribuidos.controller;

import com.unla.tp_distribuidos.model.EstadoReserva;
import com.unla.tp_distribuidos.model.Reserva;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.service.ReservaService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ReservaGraphQLController {

    private final ReservaService reservaService;

    public ReservaGraphQLController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @QueryMapping
    public List<Reserva> historialAlquileres(@Argument Long clienteId) {
        return reservaService.consultarHistorial(clienteId);
    }

    @QueryMapping
    public List<Reserva> consultarReservas(@Argument Map<String, Object> filtro) {
        Long clienteId = null;
        Long vehiculoId = null;
        TipoVehiculo tipoVehiculo = null;
        EstadoReserva estado = null;
        LocalDateTime fechaDesde = null;
        LocalDateTime fechaHasta = null;
        boolean esAdmin = false;

        if (filtro != null) {
            if (filtro.get("clienteId") != null) {
                clienteId = Long.valueOf(filtro.get("clienteId").toString());
            }
            if (filtro.get("vehiculoId") != null) {
                vehiculoId = Long.valueOf(filtro.get("vehiculoId").toString());
            }
            if (filtro.get("tipoVehiculo") != null && !((String) filtro.get("tipoVehiculo")).isBlank()) {
                tipoVehiculo = TipoVehiculo.valueOf(((String) filtro.get("tipoVehiculo")).toUpperCase());
            }
            if (filtro.get("estado") != null && !((String) filtro.get("estado")).isBlank()) {
                estado = EstadoReserva.valueOf(((String) filtro.get("estado")).toUpperCase());
            }
            if (filtro.get("fechaDesde") != null && !((String) filtro.get("fechaDesde")).isBlank()) {
                fechaDesde = LocalDateTime.parse((String) filtro.get("fechaDesde"));
            }
            if (filtro.get("fechaHasta") != null && !((String) filtro.get("fechaHasta")).isBlank()) {
                fechaHasta = LocalDateTime.parse((String) filtro.get("fechaHasta"));
            }
            if (filtro.get("esAdmin") != null) {
                esAdmin = Boolean.parseBoolean(filtro.get("esAdmin").toString());
            }
        }

        return reservaService.consultarReservas(clienteId, vehiculoId, tipoVehiculo, estado, fechaDesde, fechaHasta, esAdmin);
    }
}