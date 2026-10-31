import grpc
from grpc_clients import auth_pb2, auth_pb2_grpc

# Canal contra el puerto 9090 de Spring Boot
channel = grpc.insecure_channel("localhost:9090")
stub = auth_pb2_grpc.AuthGrpcServiceStub(channel)

def autenticar_usuario_rpc(username: str, password: str):
    request = auth_pb2.LoginRequest(username=username, password=password)
    return stub.autenticar(request)