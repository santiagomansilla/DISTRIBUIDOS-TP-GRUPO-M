package com.unla.tp_distribuidos.service;

import com.unla.tp_distribuidos.dto.ReservaDTO;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.model.EstadoMantenimiento;
import com.unla.tp_distribuidos.model.EstadoReserva;
import com.unla.tp_distribuidos.model.Reserva;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.ClienteRepository;
import com.unla.tp_distribuidos.repository.ReservaRepository;
import com.unla.tp_distribuidos.repository.VehiculoRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.ReservaConfirmadaEvent;
import java.time.Duration;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReservaService(ReservaRepository reservaRepository,
                          ClienteRepository clienteRepository,
                          VehiculoRepository vehiculoRepository,RabbitTemplate rabbitTemplate) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.rabbitTemplate = rabbitTemplate;
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


        //-----------------------------CALCULO IMPORTE TOTAL DE LA RESERVA---------------------------------
        BigDecimal importeTotal = calcularImporteTotal(dto.getFechaHoraInicio(), dto.getFechaHoraFin(), vehiculo.getPrecioDiario());

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setVehiculo(vehiculo);
        reserva.setFechaHoraInicio(dto.getFechaHoraInicio());
        reserva.setFechaHoraFin(dto.getFechaHoraFin());
        reserva.setPrecioDiario(vehiculo.getPrecioDiario()); // Congela el precio actual
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setImporteTotal(importeTotal);
        reserva.setMantenimientoResultado(EstadoMantenimiento.EN_PROCESO);
        Reserva reservaGuardada= reservaRepository.save(reserva);
        

        ReservaConfirmadaEvent EventoColaRabbit = new ReservaConfirmadaEvent(reservaGuardada.getId(),reservaGuardada.getVehiculo().getId(),reservaGuardada.getFechaHoraInicio(),reservaGuardada.getFechaHoraFin());
        //ENVIO EVENTO A LA COLA DE RABBIT 
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_RESERVA_CONFIRMADA, EventoColaRabbit);

        return reservaGuardada;
    }
    

    //UPDATE DEL ESTADO DE LA RESERVA AL CONSUMIR EL MANTENIMIENTO DE LA COLA DE RABBIT 

    public void UpdateReservaMantenimiento(Long reservaId, EstadoMantenimiento resultadoMantenimiento){

        Reserva reservaEditada= reservaRepository.findById(reservaId).orElseThrow(() -> new NoSuchElementException("Reserva no encontrada con ID: " + reservaId));

        reservaEditada.setMantenimientoResultado(resultadoMantenimiento);
        reservaRepository.save(reservaEditada);    
        System.out.println("✔ [SERVICIO RESERVA] Mantenimiento '" + resultadoMantenimiento 
                + "' registrado exitosamente para Reserva ID: " + reservaId);
    }


    public Reserva cancelarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + idReserva));

        if (reserva.getFechaHoraInicio().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Solo se pueden cancelar reservas cuya fecha de inicio sea futura");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> consultarHistorial(Long clienteId) {
    if (clienteId == null || clienteId <= 0) {
        throw new IllegalArgumentException("El clienteId es obligatorio y debe ser válido");
    }
    // Trae todas las reservas del cliente para que pueda auditar sus alquileres y cancelar las vigentes
    return reservaRepository.findByClienteId(clienteId);
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



//-------------------------------calcular cantidad de dias
public long calcularCantidadDias(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return 0;
        }
        Duration duracion = Duration.between(inicio, fin);
        long horas = duracion.toHours();
        
        // Redondeo de días hacia arriba: horas / 24 redondeado hacia el entero superior
        long dias = (long) Math.ceil((double) horas / 24.0);
        return Math.max(dias, 1L);
    }
//-------------------------------calcular importe total
public BigDecimal calcularImporteTotal(LocalDateTime inicio, LocalDateTime fin, BigDecimal precioDiario) {
        if (precioDiario == null || inicio == null || fin == null) {
            return BigDecimal.ZERO;
        }
        long dias = calcularCantidadDias(inicio, fin);
        return precioDiario.multiply(BigDecimal.valueOf(dias));
    }

}