package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.ClienteDTO;
import com.unla.tp_distribuidos.grpc.cliente.*;
import com.unla.tp_distribuidos.model.Cliente;
import com.unla.tp_distribuidos.service.ClienteService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.List;

/**
 * Customer Service — gRPC (Hito 2).
 * Responsable de operaciones relacionadas con los clientes.
 */
@GrpcService
public class ClienteGrpcServiceImpl extends ClienteGrpcServiceGrpc.ClienteGrpcServiceImplBase {

    private final ClienteService clienteService;

    public ClienteGrpcServiceImpl(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public void crearCliente(CrearClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            ClienteDTO dto = new ClienteDTO();
            if (!request.getDocumento().isBlank()) {
                dto.setDocumento(Long.parseLong(request.getDocumento()));
            }
            dto.setNombre(request.getNombre());
            dto.setApellido(request.getApellido());
            dto.setEmail(request.getEmail());
            dto.setTelefono(request.getTelefono());
            if (!request.getFechaNacimiento().isBlank()) {
                dto.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
            }

            Cliente creado = clienteService.crearCliente(dto);
            responseObserver.onNext(mapear(creado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

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
            responseObserver.onNext(mapear(modificado));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void bajaLogicaCliente(ClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            Cliente c = clienteService.obtenerPorId(request.getId());
            clienteService.bajaLogicaCliente(request.getId());
            c.setActivo(false);
            responseObserver.onNext(mapear(c));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtenerClientes(ObtenerClientesRequest request, StreamObserver<ClientesResponse> responseObserver) {
        try {
            List<Cliente> clientes = clienteService.listarTodos();
            ClientesResponse.Builder builder = ClientesResponse.newBuilder();
            for (Cliente c : clientes) {
                builder.addClientes(mapear(c));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtenerClientePorId(ClienteIdRequest request, StreamObserver<ClienteResponse> responseObserver) {
        try {
            Cliente c = clienteService.obtenerPorId(request.getId());
            responseObserver.onNext(mapear(c));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void existeCliente(ClienteIdRequest request, StreamObserver<ExisteResponse> responseObserver) {
        try {
            clienteService.obtenerPorId(request.getId());
            responseObserver.onNext(ExisteResponse.newBuilder().setExiste(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ExisteResponse.newBuilder().setExiste(false).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void clienteActivo(ClienteIdRequest request, StreamObserver<ActivoResponse> responseObserver) {
        try {
            Cliente c = clienteService.obtenerPorId(request.getId());
            responseObserver.onNext(ActivoResponse.newBuilder().setActivo(Boolean.TRUE.equals(c.getActivo())).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private ClienteResponse mapear(Cliente c) {
        return ClienteResponse.newBuilder()
                .setId(c.getId() != null ? c.getId() : 0L)
                .setNombre(c.getNombre() != null ? c.getNombre() : "")
                .setApellido(c.getApellido() != null ? c.getApellido() : "")
                .setDni(c.getDocumento() != null ? c.getDocumento().toString() : "")
                .setEmail(c.getEmail() != null ? c.getEmail() : "")
                .setActivo(c.getActivo() != null ? c.getActivo() : false)
                .build();
    }
}
