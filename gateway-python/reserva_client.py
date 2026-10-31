import sys
import os
import grpc

sys.path.append(os.path.join(os.path.dirname(__file__), "grpc_clients"))

import reserva_pb2
import reserva_pb2_grpc
from schemas import ReservaOut

# Puerto del Core Spring Boot gRPC
RESERVA_SERVICE_ADDRESS = os.getenv("RESERVA_SERVICE_ADDRESS", "localhost:9090")

# Canal y Stub persistentes para todo el cliente
_channel = grpc.insecure_channel(RESERVA_SERVICE_ADDRESS)
_stub = reserva_pb2_grpc.ReservaGrpcServiceStub(_channel)


def _to_out(r) -> ReservaOut:
    return ReservaOut(
        id=r.id,
        clienteId=r.clienteId,
        vehiculoId=r.vehiculoId,
        fechaHoraInicio=r.fechaHoraInicio,
        fechaHoraFin=r.fechaHoraFin,
        precioDiario=r.precioDiario,
        importeTotal=r.importeTotal,
        estado=r.estado,
    )


def crear(cliente_id: int, vehiculo_id: int, desde: str, hasta: str, precio_diario: float) -> ReservaOut:
    request = reserva_pb2.CrearReservaRequest(
        clienteId=cliente_id,
        vehiculoId=vehiculo_id,
        fechaHoraInicio=desde,
        fechaHoraFin=hasta,
        precioDiario=precio_diario,
    )
    metodo = getattr(_stub, "CrearReserva", None) or getattr(_stub, "crearReserva")
    return _to_out(metodo(request))


def cancelar(id: int) -> ReservaOut:
    request = reserva_pb2.ReservaIdRequest(id=id)
    metodo = getattr(_stub, "CancelarReserva", None) or getattr(_stub, "cancelarReserva")
    return _to_out(metodo(request))


def consultar_historial(cliente_id: int):
    try:
        req = reserva_pb2.ClienteIdRequest(clienteId=cliente_id)
        metodo = getattr(_stub, "ConsultarHistorial", None) or getattr(_stub, "consultarHistorial")
        response = metodo(req)
        return response.reservas
    except grpc.RpcError as e:
        print(f"[gRPC Error - consultar_historial]: {e.code()} - {e.details()}")
        raise


def consultar_reservas(
        cliente_id: int = 0, vehiculo_id: int = 0, tipo_vehiculo: str = None,
        estado: str = None, fecha_desde: str = None, fecha_hasta: str = None,
        es_admin: bool = False,
) -> list[ReservaOut]:
    request = reserva_pb2.ConsultarReservasRequest(
        clienteId=cliente_id,
        vehiculoId=vehiculo_id,
        tipoVehiculo=tipo_vehiculo or "",
        estado=estado or "",
        fechaDesde=fecha_desde or "",
        fechaHasta=fecha_hasta or "",
        esAdmin=es_admin,
    )
    metodo = getattr(_stub, "ConsultarReservas", None) or getattr(_stub, "consultarReservas")
    response = metodo(request)
    return [_to_out(r) for r in response.reservas]