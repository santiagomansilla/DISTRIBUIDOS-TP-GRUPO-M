package com.unla.tp_distribuidos.service;

import com.unla.tp_distribuidos.dto.ClienteDTO;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.repository.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente crearCliente(ClienteDTO dto) {
        if (clienteRepository.existsByDocumento(dto.getDocumento())) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento: " + dto.getDocumento());
        }

        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el email: " + dto.getEmail());
        }

        Cliente cliente = new Cliente();
        cliente.setDocumento(dto.getDocumento());
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setActivo(true);

        return clienteRepository.save(cliente);
    }

    public Cliente modificarCliente(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());

        return clienteRepository.save(cliente);
    }

    public void bajaLogicaCliente(Long id) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        cliente.darDeBaja();
        clienteRepository.save(cliente);
    }

    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
    }

    public java.util.List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
}