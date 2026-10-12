package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.VehiculoDTO;
import com.unla.tp_distribuidos.grpc.vehiculo.*;
import com.unla.tp_distribuidos.model.EstadoVehiculo;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.service.VehiculoService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vehicle Service — gRPC (Hito 2).
 * Responsable de operaciones relacionadas con los vehículos.
 */
@GrpcService
public class VehiculoGrpcServiceImpl extends VehiculoGrpcServiceGrpc.VehiculoGrpcServiceImplBase {

    private final VehiculoService vehiculoService;

    public VehiculoGrpcServiceImpl(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @Override
    public void crearVehiculo(CrearVehiculoRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            VehiculoDTO dto = new VehiculoDTO();
            dto.setPatente(request.getPatente());
            dto.setMarca(request.getMarca());
            dto.setModelo(request.getModelo());
            dto.setAnio(request.getAnio());
            dto.setColor(request.getColor());
            if (!request.getTipo().isBlank()) {
                dto.setTipo(TipoVehiculo.valueOf(request.getTipo().toUpperCase()));
            }
            dto.setPrecioDiario(BigDecimal.valueOf(request.getPrecioDiario()));
            if (!request.getEstado().isBlank()) {
                dto.setEstado(EstadoVehiculo.valueOf(request.getEstado().toUpperCase()));
            }

            Vehiculo creado = vehiculoService.crearVehiculo(dto);
            responseObserver.onNext(mapear(creado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void modificarVehiculo(ModificarVehiculoRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            VehiculoDTO dto = new VehiculoDTO();
            dto.setMarca(request.getMarca());
            dto.setModelo(request.getModelo());
            dto.setAnio(request.getAnio());
            dto.setColor(request.getColor());
            if (!request.getTipo().isBlank()) {
                dto.setTipo(TipoVehiculo.valueOf(request.getTipo().toUpperCase()));
            }
            dto.setPrecioDiario(BigDecimal.valueOf(request.getPrecioDiario()));
            if (!request.getEstado().isBlank()) {
                dto.setEstado(EstadoVehiculo.valueOf(request.getEstado().toUpperCase()));
            }

            Vehiculo modificado = vehiculoService.modificarVehiculo(request.getId(), dto);
            responseObserver.onNext(mapear(modificado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void bajaLogicaVehiculo(VehiculoIdRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            Vehiculo v = vehiculoService.obtenerPorId(request.getId());
            vehiculoService.bajaLogicaVehiculo(request.getId());
            v.setActivo(false);
            responseObserver.onNext(mapear(v));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtenerVehiculoPorId(VehiculoIdRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            Vehiculo v = vehiculoService.obtenerPorId(request.getId());
            responseObserver.onNext(mapear(v));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listarVehiculosActivos(Empty request, StreamObserver<VehiculosResponse> responseObserver) {
        try {
            List<Vehiculo> activos = vehiculoService.buscarVehiculosDisponibles();
            VehiculosResponse.Builder builder = VehiculosResponse.newBuilder();
            for (Vehiculo v : activos) {
                builder.addVehiculos(mapear(v));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void consultarDisponibilidad(DisponibilidadRequest request, StreamObserver<VehiculosResponse> responseObserver) {
        try {
            LocalDateTime inicio = LocalDateTime.parse(request.getFechaHoraInicio());
            LocalDateTime fin = LocalDateTime.parse(request.getFechaHoraFin());
            TipoVehiculo tipo = !request.getTipo().isBlank() ? TipoVehiculo.valueOf(request.getTipo().toUpperCase()) : null;
            String marca = !request.getMarca().isBlank() ? request.getMarca() : null;
            String modelo = !request.getModelo().isBlank() ? request.getModelo() : null;
            BigDecimal precioMin = request.getPrecioMin() > 0 ? BigDecimal.valueOf(request.getPrecioMin()) : null;
            BigDecimal precioMax = request.getPrecioMax() > 0 ? BigDecimal.valueOf(request.getPrecioMax()) : null;

            List<Vehiculo> disponibles = vehiculoService.buscarDisponiblesConFiltros(
                    inicio, fin, tipo, marca, modelo, precioMin, precioMax
            );

            VehiculosResponse.Builder builder = VehiculosResponse.newBuilder();
            for (Vehiculo v : disponibles) {
                builder.addVehiculos(mapear(v));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void actualizarEstadoVehiculo(ActualizarEstadoRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            EstadoVehiculo estado = EstadoVehiculo.valueOf(request.getNuevoEstado().toUpperCase());
            Vehiculo actualizado = vehiculoService.actualizarEstado(request.getId(), estado);
            responseObserver.onNext(mapear(actualizado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private VehiculoResponse mapear(Vehiculo v) {
        return VehiculoResponse.newBuilder()
                .setId(v.getId() != null ? v.getId() : 0L)
                .setPatente(v.getPatente() != null ? v.getPatente() : "")
                .setMarca(v.getMarca() != null ? v.getMarca() : "")
                .setModelo(v.getModelo() != null ? v.getModelo() : "")
                .setAnio(v.getAnio() != null ? v.getAnio() : 0)
                .setColor(v.getColor() != null ? v.getColor() : "")
                .setTipo(v.getTipo() != null ? v.getTipo().name() : "")
                .setPrecioDiario(v.getPrecioDiario() != null ? v.getPrecioDiario().doubleValue() : 0.0)
                .setEstado(v.getEstado() != null ? v.getEstado().name() : "")
                .setActivo(v.getActivo() != null ? v.getActivo() : false)
                .build();
    }
}
