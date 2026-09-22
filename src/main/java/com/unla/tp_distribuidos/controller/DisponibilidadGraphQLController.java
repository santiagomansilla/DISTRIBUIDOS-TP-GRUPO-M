package com.unla.tp_distribuidos.controller;

import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.service.VehiculoService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class DisponibilidadGraphQLController {

    private final VehiculoService vehiculoService;

    public DisponibilidadGraphQLController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @QueryMapping
    public List<Vehiculo> consultarDisponibilidad(@Argument Map<String, Object> filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException("El filtro de disponibilidad es obligatorio");
        }

        String inicioStr = (String) filtro.get("fechaHoraInicio");
        String finStr = (String) filtro.get("fechaHoraFin");

        if (inicioStr == null || finStr == null) {
            throw new IllegalArgumentException("fechaHoraInicio y fechaHoraFin son obligatorios");
        }

        LocalDateTime inicio = LocalDateTime.parse(inicioStr);
        LocalDateTime fin = LocalDateTime.parse(finStr);

        String tipoStr = (String) filtro.get("tipo");
        TipoVehiculo tipo = (tipoStr != null && !tipoStr.isBlank()) ? TipoVehiculo.valueOf(tipoStr.toUpperCase()) : null;

        String marca = (String) filtro.get("marca");
        String modelo = (String) filtro.get("modelo");

        Number pMin = (Number) filtro.get("precioMin");
        Number pMax = (Number) filtro.get("precioMax");

        BigDecimal precioMin = pMin != null ? BigDecimal.valueOf(pMin.doubleValue()) : null;
        BigDecimal precioMax = pMax != null ? BigDecimal.valueOf(pMax.doubleValue()) : null;

        return vehiculoService.buscarDisponiblesConFiltros(inicio, fin, tipo, marca, modelo, precioMin, precioMax);
    }
}