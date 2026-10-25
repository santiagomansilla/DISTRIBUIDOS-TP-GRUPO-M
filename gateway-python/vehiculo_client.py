import sys
import os

sys.path.append(os.path.join(os.path.dirname(__file__), "grpc_clients"))

import grpc
import vehiculo_pb2
import vehiculo_pb2_grpc
from schemas import VehiculoCreate, VehiculoUpdate, VehiculoOut, VehiculoExternoCreate

# Puerto a configurar (por defecto 9090 donde corre Spring Boot con los servicios gRPC)
VEHICULO_SERVICE_ADDRESS = os.getenv("VEHICULO_SERVICE_ADDRESS", "localhost:9090")


def _to_out(r) -> VehiculoOut:
    return VehiculoOut(
        id=r.id,
        patente=r.patente,
        marca=r.marca,
        modelo=r.modelo,
        anio=r.anio,
        color=r.color,
        tipo=r.tipo,
        precioDiario=r.precioDiario,
        estado=r.estado,
        activo=r.activo,
    )


def crear(dto: VehiculoCreate) -> VehiculoOut:
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        request = vehiculo_pb2.CrearVehiculoRequest(
            patente=dto.patente,
            marca=dto.marca,
            modelo=dto.modelo,
            anio=dto.anio,
            color=dto.color or "",
            tipo=dto.tipo or "",
            precioDiario=dto.precioDiario,
            estado=dto.estado or "",
        )
        return _to_out(stub.CrearVehiculo(request))


def solicitar_incorporacion_externa(dto: VehiculoExternoCreate) -> VehiculoOut:
    """
    Hito 3: Incorporación de vehículo externo (Scatter-Gather).
    Envía los datos del auto y el DNI del cliente al backend vía gRPC.
    El vehículo nace inactivo (activo = false) y dispara las consultas a Policía y DNRPA.
    """
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        request = vehiculo_pb2.SolicitudIncorporacionExternaRequest(
            patente=dto.patente.strip().upper(),
            marca=dto.marca,
            modelo=dto.modelo,
            anio=dto.anio,
            color=dto.color or "",
            tipo=dto.tipo.strip().upper() if dto.tipo else "",
            precioDiario=dto.precioDiario,
            documentoCliente=int(dto.documentoCliente),
        )
        return _to_out(stub.SolicitarIncorporacionExterna(request))


def modificar(id: int, dto: VehiculoUpdate) -> VehiculoOut:
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        request = vehiculo_pb2.ModificarVehiculoRequest(
            id=id,
            marca=dto.marca,
            modelo=dto.modelo,
            anio=dto.anio,
            color=dto.color or "",
            tipo=dto.tipo or "",
            precioDiario=dto.precioDiario,
            estado=dto.estado or "",
        )
        return _to_out(stub.ModificarVehiculo(request))


def baja_logica(id: int) -> None:
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        stub.BajaLogicaVehiculo(vehiculo_pb2.VehiculoIdRequest(id=id))


def obtener_por_id(id: int) -> VehiculoOut:
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        return _to_out(stub.ObtenerVehiculoPorId(vehiculo_pb2.VehiculoIdRequest(id=id)))


def listar_activos() -> list[VehiculoOut]:
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        response = stub.ListarVehiculosActivos(vehiculo_pb2.Empty())
        return [_to_out(v) for v in response.vehiculos]


def consultar_disponibilidad(
        fecha_inicio: str, fecha_fin: str,
        tipo: str = None, marca: str = None, modelo: str = None,
        precio_min: float = None, precio_max: float = None,
) -> list[VehiculoOut]:
    with grpc.insecure_channel(VEHICULO_SERVICE_ADDRESS) as channel:
        stub = vehiculo_pb2_grpc.VehiculoGrpcServiceStub(channel)
        request = vehiculo_pb2.DisponibilidadRequest(
            fechaHoraInicio=fecha_inicio,
            fechaHoraFin=fecha_fin,
            tipo=tipo or "",
            marca=marca or "",
            modelo=modelo or "",
            precioMin=precio_min or 0,
            precioMax=precio_max or 0,
        )
        response = stub.ConsultarDisponibilidad(request)
        return [_to_out(v) for v in response.vehiculos]