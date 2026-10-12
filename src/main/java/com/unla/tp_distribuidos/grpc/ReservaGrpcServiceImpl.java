package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.ReservaDTO;
import com.unla.tp_distribuidos.grpc.reserva.*;
import com.unla.tp_distribuidos.model.EstadoReserva;
import com.unla.tp_distribuidos.model.Reserva;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.service.ReservaService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rental Service — gRPC (Hito 2).
 * Responsable de operaciones relacionadas con las reservas y alquileres.
 */
@GrpcService
public class ReservaGrpcServiceImpl extends ReservaGrpcServiceGrpc.ReservaGrpcServiceImplBase {

    private final ReservaService reservaService;

    public ReservaGrpcServiceImpl(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Override
    public void crearReserva(CrearReservaRequest request, StreamObserver<ReservaResponse> responseObserver) {
        try {
            ReservaDTO dto = new ReservaDTO();
            dto.setClienteId(request.getClienteId());
            dto.setVehiculoId(request.getVehiculoId());
            dto.setFechaHoraInicio(LocalDateTime.parse(request.getFechaHoraInicio()));
            dto.setFechaHoraFin(LocalDateTime.parse(request.getFechaHoraFin()));

            Reserva creada = reservaService.crearReserva(dto);
            responseObserver.onNext(mapear(creada));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void cancelarReserva(ReservaIdRequest request, StreamObserver<ReservaResponse> responseObserver) {
        try {
            Reserva cancelada = reservaService.cancelarReserva(request.getId());
            responseObserver.onNext(mapear(cancelada));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void consultarHistorial(ClienteIdRequest request, StreamObserver<ReservasResponse> responseObserver) {
        try {
            List<Reserva> historial = reservaService.consultarHistorial(request.getClienteId());
            ReservasResponse.Builder builder = ReservasResponse.newBuilder();
            for (Reserva r : historial) {
                builder.addReservas(mapear(r));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void consultarReservas(ConsultarReservasRequest request, StreamObserver<ReservasResponse> responseObserver) {
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

            ReservasResponse.Builder builder = ReservasResponse.newBuilder();
            for (Reserva r : reservas) {
                builder.addReservas(mapear(r));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private ReservaResponse mapear(Reserva r) {
        return ReservaResponse.newBuilder()
                .setId(r.getId() != null ? r.getId() : 0L)
                .setClienteId(r.getCliente() != null ? r.getCliente().getId() : 0L)
                .setVehiculoId(r.getVehiculo() != null ? r.getVehiculo().getId() : 0L)
                .setFechaHoraInicio(r.getFechaHoraInicio() != null ? r.getFechaHoraInicio().toString() : "")
                .setFechaHoraFin(r.getFechaHoraFin() != null ? r.getFechaHoraFin().toString() : "")
                .setPrecioDiario(r.getPrecioDiario() != null ? r.getPrecioDiario().doubleValue() : 0.0)
                .setImporteTotal(r.getImporteTotal() != null ? r.getImporteTotal().doubleValue() : 0.0)
                .setEstado(r.getEstado() != null ? r.getEstado().name() : "")
                .build();
    }
}
