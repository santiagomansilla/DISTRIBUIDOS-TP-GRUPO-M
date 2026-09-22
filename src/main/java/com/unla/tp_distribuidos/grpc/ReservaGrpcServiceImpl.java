package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.ReservaDTO;
import com.unla.tp_distribuidos.model.EstadoReserva;
import com.unla.tp_distribuidos.model.Reserva;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.service.ReservaService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio gRPC para la gestión de Reservas (Hito 2 - RPC).
 * Implementa la lógica de creación, cancelación y consultas de reservas mediante el protocolo RPC.
 */
@GrpcService
public class ReservaGrpcServiceImpl extends ReservaGrpcServiceGrpc.ReservaGrpcServiceImplBase {

    private final ReservaService reservaService;

    public ReservaGrpcServiceImpl(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * Crea una reserva validando fechas futuras, disponibilidad del vehículo y habilitación del cliente.
     */
    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<ReservaResponse> responseObserver) {
        try {
            // Mapeo de la petición gRPC a ReservaDTO
            ReservaDTO dto = new ReservaDTO();
            dto.setClienteId(request.getClienteId());
            dto.setVehiculoId(request.getVehiculoId());
            dto.setFechaHoraInicio(LocalDateTime.parse(request.getFechaHoraInicio()));
            dto.setFechaHoraFin(LocalDateTime.parse(request.getFechaHoraFin()));

            // Ejecuta las validaciones de negocio en el servicio
            Reserva creada = reservaService.crearReserva(dto);
            responseObserver.onNext(mapearAReservaResponse(creada));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Cancela una reserva existente sólo si aún no ha comenzado el período de alquiler.
     */
    @Override
    public void cancelarReserva(CancelarReservaRequest request, StreamObserver<ReservaResponse> responseObserver) {
        try {
            Reserva cancelada = reservaService.cancelarReserva(request.getId());
            responseObserver.onNext(mapearAReservaResponse(cancelada));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Consulta el historial de alquileres finalizados o cancelados de un cliente.
     */
    @Override
    public void consultarHistorial(ConsultarHistorialRequest request, StreamObserver<ConsultarHistorialResponse> responseObserver) {
        try {
            List<Reserva> historial = reservaService.consultarHistorial(request.getClienteId());
            ConsultarHistorialResponse.Builder builder = ConsultarHistorialResponse.newBuilder();
            for (Reserva r : historial) {
                builder.addReservas(mapearAReservaResponse(r));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Consulta reservas con filtros opcionales (para clientes y administradores).
     */
    @Override
    public void consultarReservas(ConsultarReservasRequest request, StreamObserver<ConsultarReservasResponse> responseObserver) {
        try {
            Long clienteId = request.getClienteId() > 0 ? request.getClienteId() : null;
            Long vehiculoId = request.getVehiculoId() > 0 ? request.getVehiculoId() : null;
            TipoVehiculo tipo = !request.getTipoVehiculo().isBlank() ? TipoVehiculo.valueOf(request.getTipoVehiculo().toUpperCase()) : null;
            EstadoReserva estado = !request.getEstado().isBlank() ? EstadoReserva.valueOf(request.getEstado().toUpperCase()) : null;
            LocalDateTime fechaDesde = !request.getFechaDesde().isBlank() ? LocalDateTime.parse(request.getFechaDesde()) : null;
            LocalDateTime fechaHasta = !request.getFechaHasta().isBlank() ? LocalDateTime.parse(request.getFechaHasta()) : null;

            List<Reserva> reservas = reservaService.consultarReservas(
                    clienteId, vehiculoId, tipo, estado, fechaDesde, fechaHasta, request.getEsAdmin()
            );

            ConsultarReservasResponse.Builder builder = ConsultarReservasResponse.newBuilder();
            for (Reserva r : reservas) {
                builder.addReservas(mapearAReservaResponse(r));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Convierte la entidad Reserva en el mensaje de respuesta Protobuf ReservaResponse.
     */
    private ReservaResponse mapearAReservaResponse(Reserva r) {
        String clienteNombre = (r.getCliente() != null)
                ? (r.getCliente().getNombre() + " " + r.getCliente().getApellido())
                : "";
        String vehiculoPatente = (r.getVehiculo() != null) ? r.getVehiculo().getPatente() : "";
        String vehiculoDesc = (r.getVehiculo() != null)
                ? (r.getVehiculo().getMarca() + " " + r.getVehiculo().getModelo())
                : "";

        return ReservaResponse.newBuilder()
                .setId(r.getId() != null ? r.getId() : 0L)
                .setClienteId(r.getCliente() != null ? r.getCliente().getId() : 0L)
                .setClienteNombreCompleto(clienteNombre)
                .setVehiculoId(r.getVehiculo() != null ? r.getVehiculo().getId() : 0L)
                .setVehiculoPatente(vehiculoPatente)
                .setVehiculoDescripcion(vehiculoDesc)
                .setFechaHoraInicio(r.getFechaHoraInicio() != null ? r.getFechaHoraInicio().toString() : "")
                .setFechaHoraFin(r.getFechaHoraFin() != null ? r.getFechaHoraFin().toString() : "")
                .setPrecioDiario(r.getPrecioDiario() != null ? r.getPrecioDiario().doubleValue() : 0.0)
                .setImporteTotal(r.getImporteTotal() != null ? r.getImporteTotal().doubleValue() : 0.0)
                .setEstado(r.getEstado() != null ? r.getEstado().name() : "")
                .setCantidadDias(r.getCantidadDias())
                .build();
    }
}
