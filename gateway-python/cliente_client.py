import sys
import os

# config de path
sys.path.append(os.path.join(os.path.dirname(__file__), "grpc_clients"))

import grpc
import cliente_pb2
import cliente_pb2_grpc
from schemas import ClienteCreate, ClienteUpdate, ClienteOut

# Dirección del Customer Service (por defecto 9090 donde corre Spring Boot con los servicios gRPC)
CLIENTE_SERVICE_ADDRESS = os.getenv("CLIENTE_SERVICE_ADDRESS", "localhost:9090")



def _to_out(r) -> ClienteOut:
    return ClienteOut(
        id=r.id,
        documento=getattr(r, "dni", "") or getattr(r, "documento", ""),
        nombre=r.nombre,
        apellido=r.apellido,
        email=r.email,
        telefono=getattr(r, "telefono", ""),
        fechaNacimiento=getattr(r, "fechaNacimiento", ""),
        activo=r.activo,
    )



def crear(dto: ClienteCreate) -> ClienteOut:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        request = cliente_pb2.CrearClienteRequest(
            documento=dto.documento,
            nombre=dto.nombre,
            apellido=dto.apellido,
            email=dto.email,
            telefono=dto.telefono or "",
            fechaNacimiento=dto.fechaNacimiento or "",
        )
        response = stub.CrearCliente(request)
        return _to_out(response)


def modificar(id: int, dto: ClienteUpdate) -> ClienteOut:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        request = cliente_pb2.ModificarClienteRequest(
            id=id,
            nombre=dto.nombre,
            apellido=dto.apellido,
            telefono=dto.telefono or "",
            fechaNacimiento=dto.fechaNacimiento or "",
        )
        response = stub.ModificarCliente(request)
        return _to_out(response)


def baja_logica(id: int) -> None:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        stub.BajaLogicaCliente(cliente_pb2.ClienteIdRequest(id=id))


def obtener_por_id(id: int) -> ClienteOut:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        response = stub.ObtenerClientePorId(cliente_pb2.ClienteIdRequest(id=id))
        return _to_out(response)


def obtener_clientes() -> list[ClienteOut]:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        response = stub.ObtenerClientes(cliente_pb2.ObtenerClientesRequest())
        return [_to_out(c) for c in response.clientes]


def existe_cliente(id: int) -> bool:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        return stub.ExisteCliente(cliente_pb2.ClienteIdRequest(id=id)).existe


def cliente_activo(id: int) -> bool:
    with grpc.insecure_channel(CLIENTE_SERVICE_ADDRESS) as channel:
        stub = cliente_pb2_grpc.ClienteGrpcServiceStub(channel)
        return stub.ClienteActivo(cliente_pb2.ClienteIdRequest(id=id)).activo