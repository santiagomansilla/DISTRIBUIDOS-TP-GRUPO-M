import sys
import os

sys.path.append(os.path.join(os.path.dirname(__file__), "grpc_clients"))

import grpc
import reserva_pb2
import reserva_pb2_grpc
from schemas import ReservaOut

# puerto a configurar (por defecto 9090 donde corre Spring Boot con los servicios gRPC)
RESERVA_SERVICE_ADDRESS = os.getenv("RESERVA_SERVICE_ADDRESS", "localhost:9090")



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
    with grpc.insecure_channel(RESERVA_SERVICE_ADDRESS) as channel:
        stub = reserva_pb2_grpc.ReservaGrpcServiceStub(channel)
        request = reserva_pb2.CrearReservaRequest(
            clienteId=cliente_id,
            vehiculoId=vehiculo_id,
            fechaHoraInicio=desde,
            fechaHoraFin=hasta,
            precioDiario=precio_diario,
        )
        return _to_out(stub.CrearReserva(request))


def cancelar(id: int) -> ReservaOut:
    with grpc.insecure_channel(RESERVA_SERVICE_ADDRESS) as channel:
        stub = reserva_pb2_grpc.ReservaGrpcServiceStub(channel)
        return _to_out(stub.CancelarReserva(reserva_pb2.ReservaIdRequest(id=id)))


def consultar_historial(cliente_id: int) -> list[ReservaOut]:
    with grpc.insecure_channel(RESERVA_SERVICE_ADDRESS) as channel:
        stub = reserva_pb2_grpc.ReservaGrpcServiceStub(channel)
        response = stub.ConsultarHistorial(reserva_pb2.ClienteIdRequest(clienteId=cliente_id))
        return [_to_out(r) for r in response.reservas]


def consultar_reservas(
        cliente_id: int = 0, vehiculo_id: int = 0, tipo_vehiculo: str = None,
        estado: str = None, fecha_desde: str = None, fecha_hasta: str = None,
        es_admin: bool = False,
) -> list[ReservaOut]:
    with grpc.insecure_channel(RESERVA_SERVICE_ADDRESS) as channel:
        stub = reserva_pb2_grpc.ReservaGrpcServiceStub(channel)
        request = reserva_pb2.ConsultarReservasRequest(
            clienteId=cliente_id, vehiculoId=vehiculo_id,
            tipoVehiculo=tipo_vehiculo or "", estado=estado or "",
            fechaDesde=fecha_desde or "", fechaHasta=fecha_hasta or "",
            esAdmin=es_admin,
        )
        response = stub.ConsultarReservas(request)
        return [_to_out(r) for r in response.reservas]