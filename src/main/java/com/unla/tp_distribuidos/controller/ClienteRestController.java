package com.unla.tp_distribuidos.controller;

import com.unla.tp_distribuidos.dto.ClienteDTO;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Clientes", description = "Requerimiento 3: ABM y gestión de clientes (REST)")
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteRestController {

    private final ClienteService clienteService;

    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Alta de cliente", description = "Registra un nuevo cliente con documento y email únicos.")
    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crearCliente(dto));
    }

    @Operation(summary = "Modificación de cliente", description = "Actualiza los datos personales de un cliente existente.")
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> modificar(@PathVariable Long id, @Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.modificarCliente(id, dto));
    }

    @Operation(summary = "Baja lógica de cliente", description = "Deshabilita un cliente para que no pueda realizar nuevos alquileres.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> bajaLogica(@PathVariable Long id) {
        clienteService.bajaLogicaCliente(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener cliente por ID", description = "Consulta la información de un cliente activo por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @Operation(summary = "Listar todos los clientes", description = "Devuelve el listado completo de clientes registrados.")
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }
}