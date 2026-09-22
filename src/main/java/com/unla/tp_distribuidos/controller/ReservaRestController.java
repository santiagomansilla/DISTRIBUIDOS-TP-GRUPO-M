package com.unla.tp_distribuidos.controller;

import com.unla.tp_distribuidos.dto.ReservaDTO;
import com.unla.tp_distribuidos.model.Reserva;
import com.unla.tp_distribuidos.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservas", description = "Requerimientos 4 y 6: Alta y Cancelación de Reservas (REST)")
@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaRestController {

    private final ReservaService reservaService;

    public ReservaRestController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Operation(summary = "Alta de reserva", description = "Registra una reserva para un cliente y vehículo activos en un rango de fechas no solapado. Estado inicial: CONFIRMADA.")
    @PostMapping
    public ResponseEntity<Reserva> crear(@Valid @RequestBody ReservaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(dto));
    }

    @Operation(summary = "Cancelación de reserva", description = "Cancela una reserva antes de la fecha de inicio. Modifica el estado a CANCELADA sin eliminarla físicamente.")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelarReserva(id));
    }
}