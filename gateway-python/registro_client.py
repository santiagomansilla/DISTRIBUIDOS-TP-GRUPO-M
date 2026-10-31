import os
import sys
import grpc

sys.path.append(os.path.join(os.path.dirname(__file__), "grpc_clients"))

import registro_pb2
import registro_pb2_grpc

CORE_ADDRESS = os.getenv("CORE_SERVICE_ADDRESS", "localhost:9090")

def registrar(datos: dict) -> dict:
    # Validación defensiva en el cliente RPC
    if not datos.get("username") or not str(datos["username"]).strip():
        raise ValueError("El campo 'username' es obligatorio.")

    with grpc.insecure_channel(CORE_ADDRESS) as channel:
        stub = registro_pb2_grpc.RegistroGrpcServiceStub(channel)

        request = registro_pb2.RegistroRequest(
            documento=str(datos["documento"]).strip(),
            nombre=datos["nombre"].strip(),
            apellido=datos["apellido"].strip(),
            email=datos["email"].strip().lower(),
            telefono=datos.get("telefono") or "",
            fechaNacimiento=datos.get("fechaNacimiento") or "",
            password=datos["password"],
            username=datos["username"].strip()
        )

        try:
            response = stub.RegistrarCliente(request)
            return {
                "usuarioId": response.usuarioId,
                "username": response.username,
                "mensaje": response.mensaje
            }
        except grpc.RpcError as rpc_error:
            # Extrae la descripción enviada desde Spring Boot (io.grpc.Status)
            print(f"[DEBUG gRPC CODE] {rpc_error.code()}")
            print(f"[DEBUG gRPC DETAILS] {rpc_error.details()}")
            detalle = rpc_error.details() if rpc_error.details() else f"gRPC Error: {rpc_error.code()}"
            raise Exception(detalle)