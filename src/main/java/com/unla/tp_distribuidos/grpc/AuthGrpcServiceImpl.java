package com.unla.tp_distribuidos.grpc;

import com.unla.tp_distribuidos.dto.LoginResponseDTO;
import com.unla.tp_distribuidos.grpc.auth.AuthGrpcServiceGrpc.AuthGrpcServiceImplBase;
import com.unla.tp_distribuidos.grpc.auth.LoginRequest;
import com.unla.tp_distribuidos.grpc.auth.LoginResponse;
import com.unla.tp_distribuidos.service.AuthService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AuthGrpcServiceImpl extends AuthGrpcServiceImplBase {

    private final AuthService authService;

    // Delegación limpia a la capa de servicio
    public AuthGrpcServiceImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void autenticar(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            LoginResponseDTO dto = authService.autenticar(request.getUsername(), request.getPassword());

            LoginResponse response = LoginResponse.newBuilder()
                    .setAutenticado(true)
                    .setUsername(dto.getUsername())
                    .setRol(dto.getRol())
                    .setClienteId(dto.getClienteId() != null ? dto.getClienteId() : 0L)
                    .setMensaje("Autenticación exitosa")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            LoginResponse response = LoginResponse.newBuilder()
                    .setAutenticado(false)
                    .setMensaje(e.getMessage() != null ? e.getMessage() : "Error de autenticación")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}