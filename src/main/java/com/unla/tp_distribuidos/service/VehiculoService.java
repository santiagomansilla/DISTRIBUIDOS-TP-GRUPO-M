package com.unla.tp_distribuidos.service;

import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.VehiculoDTO;
import com.unla.tp_distribuidos.dto.VehiculoVerificacionSolicitadaEvent;
import com.unla.tp_distribuidos.dto.TitularidadSolicitudDTO;
import com.unla.tp_distribuidos.model.EstadoVehiculo;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.ReservaRepository;
import com.unla.tp_distribuidos.repository.VehiculoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final RabbitTemplate rabbitTemplate;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           ReservaRepository reservaRepository,
                           RabbitTemplate rabbitTemplate) {
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * HITO 1 / 2: Alta de flota propia (Administrador).
     * Nace disponible y activo. Dispara verificación preventiva policial.
     */
    @Transactional
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

        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        // Disparo asíncrono policial para flota propia (no toca DNRPA)
        VehiculoVerificacionSolicitadaEvent eventoPolicia = new VehiculoVerificacionSolicitadaEvent(
                guardado.getId(),
                guardado.getPatente(),
                "FLOTA_PROPIA"
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_VERIF_SOLICITADA, eventoPolicia);

        return guardado;
    }

    /**
     * HITO 3: Solicitud de incorporación de vehículo externo (Cliente).
     * Patrón Scatter: Nace inactivo (activo = false) y emite en paralelo a Policía y DNRPA.
     */
    @Transactional
    public Vehiculo solicitarIncorporacionExterna(VehiculoDTO dto, Long documentoCliente) {
        if (vehiculoRepository.existsByPatente(dto.getPatente())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la patente: " + dto.getPatente());
        }
        if (documentoCliente == null) {
            throw new IllegalArgumentException("El documento del cliente titular es obligatorio para verificar la titularidad.");
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente(dto.getPatente());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setColor(dto.getColor());
        vehiculo.setTipo(dto.getTipo());
        vehiculo.setPrecioDiario(dto.getPrecioDiario());
        vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActivo(false); // Inhabilitado hasta que el agregador reciba ambas aprobaciones

        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        // 1. Rama Policía (Scatter)
        VehiculoVerificacionSolicitadaEvent eventoPolicia = new VehiculoVerificacionSolicitadaEvent(
                guardado.getId(),
                guardado.getPatente(),
                "INCORPORACION_EXTERNA"
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_VERIF_SOLICITADA, eventoPolicia);

        // 2. Rama DNRPA (Scatter)
        TitularidadSolicitudDTO eventoDnrpa = new TitularidadSolicitudDTO(
                guardado.getId(),
                guardado.getPatente(),
                documentoCliente
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_TITULARIDAD_SOLICITADA, eventoDnrpa);

        System.out.println("[SCATTER DISPARADO] Solicitudes enviadas en paralelo para vehículo ID: " 
                + guardado.getId() + " (Patente: " + guardado.getPatente() + ")");

        return guardado;
    }

    public Vehiculo modificarVehiculo(Long id, VehiculoDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));

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
            LocalDateTime inicio,
            LocalDateTime fin,
            TipoVehiculo tipo,
            String marca,
            String modelo,
            BigDecimal precioMin,
            BigDecimal precioMax) {

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

    public Vehiculo actualizarEstado(Long id, EstadoVehiculo nuevoEstado) {
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + id));
        vehiculo.setEstado(nuevoEstado);
        return vehiculoRepository.save(vehiculo);
    }
}