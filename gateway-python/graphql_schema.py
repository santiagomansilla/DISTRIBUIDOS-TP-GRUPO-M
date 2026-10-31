import strawberry
from typing import Optional, List
from datetime import datetime

import vehiculo_client
import cliente_client
import reserva_client


# =====================================================================
# TIPOS GRAPHQL
# =====================================================================

@strawberry.type(description="Vehículo disponible para alquiler durante el rango de fechas (Requerimiento 2)")
class VehiculoDisponibleType:
    id: int = strawberry.field(description="Identificador único del vehículo")
    patente: str = strawberry.field(description="Patente del vehículo")
    marca: str = strawberry.field(description="Marca del fabricante")
    modelo: str = strawberry.field(description="Modelo comercial")
    anio: int = strawberry.field(description="Año de fabricación")
    color: str = strawberry.field(description="Color del rodado")
    tipo: str = strawberry.field(description="Tipo: SEDAN, SUV, PICKUP, COUPE o HATCHBACK")
    precioDiario: float = strawberry.field(description="Tarifa diaria de alquiler")


@strawberry.type(description="Detalle completo de una reserva (Requerimiento 5)")
class ReservaDetalleType:
    id: int = strawberry.field(description="ID de la reserva")
    clienteNombre: str = strawberry.field(description="Nombre del cliente")
    clienteApellido: str = strawberry.field(description="Apellido del cliente")
    vehiculoMarca: str = strawberry.field(description="Marca del vehículo")
    vehiculoModelo: str = strawberry.field(description="Modelo del vehículo")
    patente: str = strawberry.field(description="Patente del vehículo")
    fechaHoraInicio: str = strawberry.field(description="Fecha y hora pactada de inicio")
    fechaHoraFin: str = strawberry.field(description="Fecha y hora pactada de entrega")
    precioDiario: float = strawberry.field(description="Precio diario congelado de la reserva")
    importeTotal: float = strawberry.field(description="Importe total facturado")
    estado: str = strawberry.field(description="Estado de la reserva: CONFIRMADA, FINALIZADA o CANCELADA")


@strawberry.type(description="Historial de alquileres del cliente (Requerimiento 7)")
class HistorialType:
    id: int = strawberry.field(description="ID de reserva utilizado para cancelación (Req 6)")
    vehiculoMarca: str = strawberry.field(description="Marca del vehículo")
    vehiculoModelo: str = strawberry.field(description="Modelo del vehículo")
    patente: str = strawberry.field(description="Patente del vehículo")
    fechaHoraInicio: str = strawberry.field(description="Fecha y hora de inicio")
    fechaHoraFin: str = strawberry.field(description="Fecha y hora de finalización")
    cantidadDias: int = strawberry.field(description="Duración total en días")
    importeTotal: float = strawberry.field(description="Monto liquidado")
    estado: str = strawberry.field(description="Estado actual de la operación")


# =====================================================================
# ENRIQUECEDORES (Orquestación gRPC entre Microservicios)
# =====================================================================

def _enriquecer_historial(r) -> HistorialType:
    vehiculo = vehiculo_client.obtener_por_id(r.vehiculoId)
    inicio = datetime.fromisoformat(r.fechaHoraInicio)
    fin = datetime.fromisoformat(r.fechaHoraFin)
    dias = max((fin.date() - inicio.date()).days, 1)

    return HistorialType(
        id=int(r.id),
        vehiculoMarca=str(vehiculo.marca),
        vehiculoModelo=str(vehiculo.modelo),
        patente=str(vehiculo.patente),
        fechaHoraInicio=str(r.fechaHoraInicio),
        fechaHoraFin=str(r.fechaHoraFin),
        cantidadDias=int(dias),
        importeTotal=float(r.importeTotal),
        estado=str(r.estado),
    )


def _enriquecer_reserva(r) -> ReservaDetalleType:
    vehiculo = vehiculo_client.obtener_por_id(r.vehiculoId)
    cliente = cliente_client.obtener_por_id(r.clienteId)

    return ReservaDetalleType(
        id=int(r.id),
        clienteNombre=str(cliente.nombre),
        clienteApellido=str(cliente.apellido),
        vehiculoMarca=str(vehiculo.marca),
        vehiculoModelo=str(vehiculo.modelo),
        patente=str(vehiculo.patente),
        fechaHoraInicio=str(r.fechaHoraInicio),
        fechaHoraFin=str(r.fechaHoraFin),
        precioDiario=float(r.precioDiario),
        importeTotal=float(r.importeTotal),
        estado=str(r.estado),
    )


# =====================================================================
# QUERY ROOT (Requerimientos 2, 5 y 7)
# =====================================================================

@strawberry.type
class Query:

    @strawberry.field(description="Requerimiento 2: Vehículos disponibles en un rango de fechas con filtros opcionales.")
    def disponibilidad(
        self,
        fechaHoraInicio: str,
        fechaHoraFin: str,
        tipo: Optional[str] = None,
        marca: Optional[str] = None,
        modelo: Optional[str] = None,
        precioMin: Optional[float] = None,
        precioMax: Optional[float] = None,
    ) -> List[VehiculoDisponibleType]:
        vehiculos = vehiculo_client.consultar_disponibilidad(
            fechaHoraInicio, fechaHoraFin, tipo, marca, modelo, precioMin, precioMax
        )
        return [
            VehiculoDisponibleType(
                id=v.id,
                patente=v.patente,
                marca=v.marca,
                modelo=v.modelo,
                anio=v.anio,
                color=v.color or "-",
                tipo=v.tipo,
                precioDiario=v.precioDiario,
            )
            for v in vehiculos
        ]

    @strawberry.field(description="Requerimiento 5: Consulta de reservas. Clientes ven solo las propias; admin ve todas.")
    def reservas(
        self,
        esAdmin: bool,
        clienteId: Optional[int] = None,
        vehiculoId: Optional[int] = None,
        tipoVehiculo: Optional[str] = None,
        estado: Optional[str] = None,
        fechaDesde: Optional[str] = None,
        fechaHasta: Optional[str] = None,
    ) -> List[ReservaDetalleType]:
        filtro_cliente = clienteId if esAdmin else (clienteId or 0)
        reservas_lista = reserva_client.consultar_reservas(
            cliente_id=filtro_cliente or 0,
            vehiculo_id=vehiculoId or 0,
            tipo_vehiculo=tipoVehiculo,
            estado=estado,
            fecha_desde=fechaDesde,
            fecha_hasta=fechaHasta,
            es_admin=esAdmin,
        )
        return [_enriquecer_reserva(r) for r in reservas_lista]

    @strawberry.field(description="Requerimiento 7: Historial de alquileres y reservas de un cliente.")
    def historial(self, clienteId: int) -> List[HistorialType]:
        reservas_lista = reserva_client.consultar_historial(clienteId)
        return [_enriquecer_historial(r) for r in reservas_lista]


schema = strawberry.Schema(query=Query)