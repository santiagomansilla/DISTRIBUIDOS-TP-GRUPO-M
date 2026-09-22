package com.unla.tp_distribuidos.controller;

import com.unla.tp_distribuidos.dto.VehiculoDTO;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vehículos", description = "Requerimiento 1: ABM y gestión de vehículos (REST)")
@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoRestController {

    private final VehiculoService vehiculoService;

    public VehiculoRestController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @Operation(summary = "Alta de vehículo", description = "Registra un nuevo vehículo en la flota. Queda inicialmente en estado DISPONIBLE.")
    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody VehiculoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crearVehiculo(dto));
    }

    @Operation(summary = "Modificación de vehículo", description = "Actualiza los datos de un vehículo existente. No se permite modificar la patente.")
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> modificar(@PathVariable Long id, @Valid @RequestBody VehiculoDTO dto) {
        return ResponseEntity.ok(vehiculoService.modificarVehiculo(id, dto));
    }

    @Operation(summary = "Baja lógica de vehículo", description = "Desactiva un vehículo para que no pueda ser utilizado en nuevos alquileres.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> bajaLogica(@PathVariable Long id) {
        vehiculoService.bajaLogicaVehiculo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener vehículo por ID", description = "Consulta la información detallada de un vehículo activo por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @Operation(summary = "Listar vehículos activos", description = "Devuelve todos los vehículos habilitados en la flota.")
    @GetMapping
    public ResponseEntity<List<Vehiculo>> listarDisponibles() {
        return ResponseEntity.ok(vehiculoService.buscarVehiculosDisponibles());
    }
}