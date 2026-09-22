package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.VehiculoDTO;
import com.unla.tp_distribuidos.model.EstadoVehiculo;
import com.unla.tp_distribuidos.model.TipoVehiculo;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.service.VehiculoService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio gRPC para la gestión de Vehículos (Hito 2 - RPC).
 * Implementa las operaciones definidas en el contrato 'vehiculo.proto'.
 * Anotado con @GrpcService para que el starter de gRPC lo registre automáticamente en el servidor RPC.
 */
@GrpcService
public class VehiculoGrpcServiceImpl extends VehiculoGrpcServiceGrpc.VehiculoGrpcServiceImplBase {

    private final VehiculoService vehiculoService;

    public VehiculoGrpcServiceImpl(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    /**
     * Da de alta un vehículo en el sistema vía gRPC.
     * @param request Datos del vehículo a crear enviados por el cliente gRPC.
     * @param responseObserver Observador de flujo para emitir la respuesta asíncrona o error.
     */
    @Override
    public void crearVehiculo(CrearVehiculoRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            // Mapeo del mensaje protobuf al DTO de negocio
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

            // Delegación de la lógica de negocio al servicio
            Vehiculo creado = vehiculoService.crearVehiculo(dto);

            // Respuesta exitosa al cliente gRPC
            responseObserver.onNext(mapearAVehiculoResponse(creado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Manejo y retorno de error estructurado de gRPC
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Modifica los datos de un vehículo existente. La patente no se modifica.
     */
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
            responseObserver.onNext(mapearAVehiculoResponse(modificado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Aplica la baja lógica de un vehículo marcándolo como inactivo.
     */
    @Override
    public void bajaLogicaVehiculo(BajaLogicaVehiculoRequest request, StreamObserver<BajaLogicaVehiculoResponse> responseObserver) {
        try {
            vehiculoService.bajaLogicaVehiculo(request.getId());
            BajaLogicaVehiculoResponse resp = BajaLogicaVehiculoResponse.newBuilder()
                    .setExito(true)
                    .setMensaje("Vehículo dado de baja correctamente")
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Consulta un vehículo activo por su ID.
     */
    @Override
    public void obtenerVehiculoPorId(ObtenerVehiculoPorIdRequest request, StreamObserver<VehiculoResponse> responseObserver) {
        try {
            Vehiculo v = vehiculoService.obtenerPorId(request.getId());
            responseObserver.onNext(mapearAVehiculoResponse(v));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Obtiene el listado de vehículos activos disponibles.
     */
    @Override
    public void listarVehiculosDisponibles(ListarVehiculosDisponiblesRequest request, StreamObserver<ListarVehiculosResponse> responseObserver) {
        try {
            List<Vehiculo> disponibles = vehiculoService.buscarVehiculosDisponibles();
            ListarVehiculosResponse.Builder builder = ListarVehiculosResponse.newBuilder();
            for (Vehiculo v : disponibles) {
                builder.addVehiculos(mapearAVehiculoResponse(v));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Convierte una entidad de dominio Vehiculo al mensaje protobuf VehiculoResponse.
     */
    private VehiculoResponse mapearAVehiculoResponse(Vehiculo v) {
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
