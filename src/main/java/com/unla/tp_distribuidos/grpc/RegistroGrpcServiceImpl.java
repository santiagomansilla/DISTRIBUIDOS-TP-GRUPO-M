package com.unla.tp_distribuidos.grpc;
import com.unla.tp_distribuidos.dto.RegistroRequestDto;
import com.unla.tp_distribuidos.dto.RegistroResponseDto;
import com.unla.tp_distribuidos.grpc.auth.AuthGrpcServiceGrpc.AuthGrpcServiceImplBase;
import com.unla.tp_distribuidos.grpc.cliente.ClienteResponse;
import com.unla.tp_distribuidos.grpc.registro.RegistroGrpcServiceGrpc;
import com.unla.tp_distribuidos.grpc.registro.RegistroRequest;
import com.unla.tp_distribuidos.grpc.registro.RegistroResponse;
import com.unla.tp_distribuidos.service.RegistroService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.unla.tp_distribuidos.model.Usuario;
import io.grpc.Status;
@GrpcService
public class RegistroGrpcServiceImpl extends RegistroGrpcServiceGrpc.RegistroGrpcServiceImplBase  {
    
    private final RegistroService registroService;

    public RegistroGrpcServiceImpl(RegistroService registroService) {
        this.registroService = registroService;
    }   
    @Override 
    public void registrarCliente(RegistroRequest request, StreamObserver<RegistroResponse> responseObserver) {
        try {
            
            RegistroRequestDto dto = new RegistroRequestDto();
            dto.setDocumento(request.getDocumento().trim());
            dto.setNombre(request.getNombre().trim());
            dto.setApellido(request.getApellido().trim());
            dto.setEmail(request.getEmail().trim());
            dto.setTelefono(request.getTelefono().trim());
            dto.setFechaNacimiento(request.getFechaNacimiento().trim());
            dto.setUsername(request.getUsername().trim());
            dto.setPassword(request.getPassword().trim());

            Usuario usuario = registroService.crearUsuario(dto);

            // Construir la respuesta gRPC
            responseObserver.onNext(mapear(usuario));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Manejar errores y enviar una respuesta de error
            e.printStackTrace(); //  traza en la terminal de Spring Boot
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error interno en backend: " + e.getMessage())
                    .asRuntimeException());
        }
    }

       private RegistroResponse mapear(Usuario usuario) {
        return RegistroResponse.newBuilder()
                .setUsername(usuario.getUsername())
                .setUsuarioId(usuario.getId())
                .setMensaje("Usuario registrado exitosamente")
                .build();
    }
}

    


      

