package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.ClienteDTO;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.service.ClienteService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio gRPC para la gestión de Clientes (Hito 2 - RPC).
 * Implementa las operaciones definidas en 'cliente.proto'.
 */
@GrpcService
public class ClienteGrpcServiceImpl extends ClienteGrpcServiceGrpc.ClienteGrpcServiceImplBase {

    private final ClienteService clienteService;

    public ClienteGrpcServiceImpl(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Da de alta un nuevo cliente asegurando la unicidad de documento y email.
     */
    @Override
    public void crearCliente(CrearClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            // Mapeo del mensaje protobuf al DTO de cliente
            ClienteDTO dto = new ClienteDTO();
            dto.setDocumento(request.getDocumento());
            dto.setNombre(request.getNombre());
            dto.setApellido(request.getApellido());
            dto.setEmail(request.getEmail());
            dto.setTelefono(request.getTelefono());
            if (!request.getFechaNacimiento().isBlank()) {
                dto.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            }

            Cliente creado = clienteService.crearCliente(dto);
            responseObserver.onNext(mapearAClienteResponse(creado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Modifica los datos personales de un cliente existente.
     */
    @Override
    public void modificarCliente(ModificarClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            ClienteDTO dto = new ClienteDTO();
            dto.setNombre(request.getNombre());
            dto.setApellido(request.getApellido());
            dto.setTelefono(request.getTelefono());
            if (!request.getFechaNacimiento().isBlank()) {
                dto.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            }

            Cliente modificado = clienteService.modificarCliente(request.getId(), dto);
            responseObserver.onNext(mapearAClienteResponse(modificado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Inhabilita a un cliente para que no pueda realizar nuevos alquileres.
     */
    @Override
    public void bajaLogicaCliente(BajaLogicaClienteRequest request, StreamObserver<BajaLogicaClienteResponse> responseObserver) {
        try {
            clienteService.bajaLogicaCliente(request.getId());
            BajaLogicaClienteResponse resp = BajaLogicaClienteResponse.newBuilder()
                    .setExito(true)
                    .setMensaje("Cliente dado de baja correctamente")
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Busca un cliente activo por su identificador primario.
     */
    @Override
    public void obtenerClientePorId(ObtenerClientePorIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            Cliente c = clienteService.obtenerPorId(request.getId());
            responseObserver.onNext(mapearAClienteResponse(c));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Lista todos los clientes registrados en la base de datos.
     */
    @Override
    public void listarClientes(ListarClientesRequest request, StreamObserver<ListarClientesResponse> responseObserver) {
        try {
            List<Cliente> clientes = clienteService.listarTodos();
            ListarClientesResponse.Builder builder = ListarClientesResponse.newBuilder();
            for (Cliente c : clientes) {
                builder.addClientes(mapearAClienteResponse(c));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    /**
     * Convierte la entidad Cliente a la estructura de respuesta Protobuf.
     */
    private ClienteResponse mapearAClienteResponse(Cliente c) {
        return ClienteResponse.newBuilder()
                .setId(c.getId() != null ? c.getId() : 0L)
                .setDocumento(c.getDocumento() != null ? c.getDocumento() : 0L)
                .setNombre(c.getNombre() != null ? c.getNombre() : "")
                .setApellido(c.getApellido() != null ? c.getApellido() : "")
                .setEmail(c.getEmail() != null ? c.getEmail() : "")
                .setTelefono(c.getTelefono() != null ? c.getTelefono() : "")
                .setFechaNacimiento(c.getFechaNacimiento() != null ? c.getFechaNacimiento().toString() : "")
                .setActivo(c.getActivo() != null ? c.getActivo() : false)
                .build();
    }
}
