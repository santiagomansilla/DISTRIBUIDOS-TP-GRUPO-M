package com.unla.tp_distribuidos.service;

import com.unla.tp_distribuidos.dto.ReservaDTO;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.model.EstadoReserva;
import com.unla.tp_distribuidos.model.Reserva;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.ClienteRepository;
import com.unla.tp_distribuidos.repository.ReservaRepository;
import com.unla.tp_distribuidos.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          ClienteRepository clienteRepository,
                          VehiculoRepository vehiculoRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    public Reserva crearReserva(ReservaDTO dto) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(dto.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado o inactivo"));

        if (!cliente.puedeAlquilar()) {
            throw new IllegalStateException("El cliente no está habilitado para realizar reservas");
        }

        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(dto.getVehiculoId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado o inactivo"));

        if (dto.getFechaHoraInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de inicio de la reserva debe ser futura");
        }

        if (dto.getFechaHoraFin().isBefore(dto.getFechaHoraInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        boolean existeSolapamiento = reservaRepository.existeSolapamiento(
                vehiculo.getId(), dto.getFechaHoraInicio(), dto.getFechaHoraFin());

        if (existeSolapamiento) {
            throw new IllegalStateException("El vehículo no se encuentra disponible en las fechas solicitadas");
        }

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVehiculo(vehiculo);
        reserva.setFechaHoraInicio(dto.getFechaHoraInicio());
        reserva.setFechaHoraFin(dto.getFechaHoraFin());
        reserva.setPrecioDiario(vehiculo.getPrecioDiario()); // Congela el precio actual
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.calcularImporteTotal();

        return reservaRepository.save(reserva);
    }

    public Reserva cancelarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + idReserva));

        if (reserva.getFechaHoraInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Solo se pueden cancelar reservas cuya fecha de inicio sea futura");
        }

        reserva.cancelar();
        return reservaRepository.save(reserva);
    }

    public List<Reserva> consultarHistorial(Long clienteId) {
        return reservaRepository.findByClienteId(clienteId).stream()
                .filter(r -> r.getEstado() == EstadoReserva.FINALIZADA || r.getEstado() == EstadoReserva.CANCELADA)
                .toList();
    }

    public List<Reserva> consultarReservas(Long clienteId, Long vehiculoId,
                                           com.unla.tp_distribuidos.model.TipoVehiculo tipoVehiculo,
                                           EstadoReserva estado,
                                           LocalDateTime fechaDesde,
                                           LocalDateTime fechaHasta,
                                           boolean esAdmin) {
        List<Reserva> reservas;
        if (esAdmin) {
            reservas = reservaRepository.findAll();
        } else {
            if (clienteId == null) {
                throw new IllegalArgumentException("El clienteId es obligatorio para usuarios no administradores");
            }
            reservas = reservaRepository.findByClienteId(clienteId);
        }

        return reservas.stream()
                .filter(r -> !esAdmin || clienteId == null || r.getCliente().getId().equals(clienteId))
                .filter(r -> vehiculoId == null || r.getVehiculo().getId().equals(vehiculoId))
                .filter(r -> tipoVehiculo == null || r.getVehiculo().getTipo() == tipoVehiculo)
                .filter(r -> estado == null || r.getEstado() == estado)
                .filter(r -> fechaDesde == null || !r.getFechaHoraInicio().isBefore(fechaDesde))
                .filter(r -> fechaHasta == null || !r.getFechaHoraFin().isAfter(fechaHasta))
                .toList();
    }
}