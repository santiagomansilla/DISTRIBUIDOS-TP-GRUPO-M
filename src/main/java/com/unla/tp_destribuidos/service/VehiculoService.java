package com.unla.tp_distribuidos.service;

import com.unla.tp_distribuidos.dto.VehiculoDTO;
import com.unla.tp_distribuidos.model.EstadoVehiculo;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final com.unla.tp_distribuidos.repository.ReservaRepository reservaRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           com.unla.tp_distribuidos.repository.ReservaRepository reservaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
    }

    public Vehiculo crearVehiculo(VehiculoDTO dto) {
        if (vehiculoRepository.existsByPatente(dto.getPatente())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la patente: " + dto.getPatente());
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(dto.getPatente());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setColor(dto.getColor());
        vehiculo.setTipo(dto.getTipo());
        vehiculo.setPrecioDiario(dto.getPrecioDiario());
        vehiculo.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(true);

        return vehiculoRepository.save(vehiculo);
    }

    public Vehiculo modificarVehiculo(Long id, VehiculoDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));

        // Regla: no se permite modificar la patente
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setColor(dto.getColor());
        vehiculo.setTipo(dto.getTipo());
        vehiculo.setPrecioDiario(dto.getPrecioDiario());
        if (dto.getEstado() != null) {
            vehiculo.setEstado(dto.getEstado());
        }

        return vehiculoRepository.save(vehiculo);
    }

    public void bajaLogicaVehiculo(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
    }

    public Vehiculo obtenerPorId(Long id) {
        return vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
    }

    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    public List<Vehiculo> buscarVehiculosDisponibles() {
        return vehiculoRepository.findByActivoTrue();
    }

    public List<Vehiculo> buscarDisponiblesConFiltros(
            java.time.LocalDateTime inicio,
            java.time.LocalDateTime fin,
            com.unla.tp_distribuidos.model.TipoVehiculo tipo,
            String marca,
            String modelo,
            java.math.BigDecimal precioMin,
            java.math.BigDecimal precioMax) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("La fecha y hora de inicio y finalización son obligatorias");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha y hora de fin debe ser posterior a la de inicio");
        }

        List<Long> idsOcupados = reservaRepository.findVehiculosOcupadosEnRango(inicio, fin);

        return vehiculoRepository.findByActivoTrue().stream()
                .filter(v -> !idsOcupados.contains(v.getId()))
                .filter(v -> tipo == null || v.getTipo() == tipo)
                .filter(v -> marca == null || marca.isBlank() || v.getMarca().equalsIgnoreCase(marca))
                .filter(v -> modelo == null || modelo.isBlank() || v.getModelo().equalsIgnoreCase(modelo))
                .filter(v -> precioMin == null || (v.getPrecioDiario() != null && v.getPrecioDiario().compareTo(precioMin) >= 0))
                .filter(v -> precioMax == null || (v.getPrecioDiario() != null && v.getPrecioDiario().compareTo(precioMax) <= 0))
                .toList();
    }
}