from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from datetime import datetime
from strawberry.fastapi import GraphQLRouter
from graphql_schema import schema
from pydantic import BaseModel
import grpc

import cliente_client
import vehiculo_client
import reserva_client
import auth_client
from schemas import (
    ClienteCreate, ClienteUpdate, ClienteOut,
    VehiculoCreate, VehiculoUpdate, VehiculoOut, VehiculoExternoCreate,
    ReservaCreate, ReservaOut
)

from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="Rentar - API Gateway",
    description="Punto de entrada único al sistema. Habla con los servicios internos por gRPC.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class LoginDTO(BaseModel):
    username: str
    password: str


@app.post("/api/auth/login", tags=["Autenticación"])
def login_endpoint(datos: LoginDTO):
    try:
        res = auth_client.autenticar_usuario_rpc(datos.username, datos.password)
        
        if not res.autenticado:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=res.mensaje
            )

        return {
            "username": res.username,
            "rol": res.rol,
            "clienteId": res.clienteId if res.clienteId != 0 else None,
            "token": f"jwt-session-{res.username}"
        }
    except grpc.RpcError as e:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Error en comunicación gRPC con Core: {e.details()}"
        )

# Traduce errores de gRPC (vienen del servicio interno) a códigos HTTP entendibles
# para la interfaz web
def grpc_error_to_http(e: grpc.RpcError):
    code = e.code()
    detail = e.details()
    if code == grpc.StatusCode.NOT_FOUND:
        raise HTTPException(status_code=404, detail=detail)
    if code == grpc.StatusCode.INVALID_ARGUMENT:
        raise HTTPException(status_code=400, detail=detail)
    raise HTTPException(status_code=502, detail=f"Error de comunicación con el servicio interno: {detail}")


@app.post("/api/v1/clientes", response_model=ClienteOut, status_code=201)
def crear_cliente(dto: ClienteCreate):
    try:
        return cliente_client.crear(dto)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.put("/api/v1/clientes/{id}", response_model=ClienteOut)
def modificar_cliente(id: int, dto: ClienteUpdate):
    try:
        return cliente_client.modificar(id, dto)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.delete("/api/v1/clientes/{id}", status_code=204)
def baja_logica_cliente(id: int):
    try:
        cliente_client.baja_logica(id)
        return JSONResponse(status_code=204, content=None)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.get("/api/v1/clientes/{id}", response_model=ClienteOut)
def obtener_cliente(id: int):
    try:
        return cliente_client.obtener_por_id(id)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.get("/api/v1/clientes", response_model=list[ClienteOut])
def listar_clientes():
    try:
        return cliente_client.obtener_clientes()
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.get("/api/v1/clientes/{id}/existe")
def verificar_existencia_cliente(id: int):
    try:
        cliente_client.obtener_por_id(id)
        return {"resultado": True}
    except grpc.RpcError as e:
        if e.code() == grpc.StatusCode.NOT_FOUND:
            return {"resultado": False}
        grpc_error_to_http(e)


@app.get("/api/v1/clientes/{id}/activo")
def verificar_cliente_activo(id: int):
    try:
        activo = cliente_client.cliente_activo(id)
        return {"activo": bool(activo)}
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.post("/api/v1/clientes/validar")
def validar_cliente(dto: dict):
    cliente_id = dto.get("clienteId")

    if not isinstance(cliente_id, int) or isinstance(cliente_id, bool) or cliente_id <= 0:
        raise HTTPException(
            status_code=400,
            detail="El ID del cliente debe ser un entero positivo"
        )

    try:
        cliente = cliente_client.obtener_por_id(cliente_id)
        activo = cliente_client.cliente_activo(cliente_id)

        if not activo:
            return {
                "valido": False,
                "mensaje": "El cliente existe, pero no está activo."
            }

        return {
            "valido": True,
            "mensaje": "El cliente existe y está activo."
        }

    except grpc.RpcError as e:
        if e.code() == grpc.StatusCode.NOT_FOUND:
            return {
                "valido": False,
                "mensaje": "El cliente no existe."
            }
        grpc_error_to_http(e)

@app.post("/api/v1/vehiculos", response_model=VehiculoOut, status_code=201)
def crear_vehiculo(dto: VehiculoCreate):
    try:
        return vehiculo_client.crear(dto)
    except grpc.RpcError as e:
        grpc_error_to_http(e)

@app.post("/api/v1/vehiculos/externos", response_model=VehiculoOut, status_code=202)
def solicitar_incorporacion_externa(dto: VehiculoExternoCreate):
    """
    Hito 3: Incorporación asíncrona de vehículo externo (Scatter-Gather).
    Persiste el vehículo inactivo (activo = false) y dispara las validaciones
    en paralelo hacia Policía y DNRPA vía RabbitMQ.
    """
    try:
        return vehiculo_client.solicitar_incorporacion_externa(dto)
    except grpc.RpcError as e:
        grpc_error_to_http(e)

        
@app.put("/api/v1/vehiculos/{id}", response_model=VehiculoOut)
def modificar_vehiculo(id: int, dto: VehiculoUpdate):
    try:
        return vehiculo_client.modificar(id, dto)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.delete("/api/v1/vehiculos/{id}", status_code=204)
def baja_logica_vehiculo(id: int):
    try:
        vehiculo_client.baja_logica(id)
        return JSONResponse(status_code=204, content=None)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.get("/api/v1/vehiculos/{id}", response_model=VehiculoOut)
def obtener_vehiculo(id: int):
    try:
        return vehiculo_client.obtener_por_id(id)
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.get("/api/v1/vehiculos", response_model=list[VehiculoOut])
def listar_vehiculos():
    try:
        return vehiculo_client.listar_activos()
    except grpc.RpcError as e:
        grpc_error_to_http(e)

@app.post("/api/v1/reservas", response_model=ReservaOut, status_code=201)
def crear_reserva(dto: ReservaCreate):

    # 1) Validar cliente: existe y está activo (llamada al Customer Service)
    try:
        if not cliente_client.cliente_activo(dto.clienteId):
            raise HTTPException(status_code=400, detail="El cliente no está activo")
    except grpc.RpcError as e:
        grpc_error_to_http(e)

    # 2) Validar fechas
    try:
        inicio = datetime.fromisoformat(dto.fechaHoraInicio)
        fin = datetime.fromisoformat(dto.fechaHoraFin)
    except ValueError:
        raise HTTPException(status_code=400, detail="Formato de fecha inválido, use ISO-8601")

    if inicio < datetime.now():
        raise HTTPException(status_code=400, detail="La fecha de inicio debe ser futura")
    if fin < inicio:
        raise HTTPException(status_code=400, detail="La fecha de fin debe ser posterior a la de inicio")

    # 3) Validar vehículo: existe, está activo y disponible en esas fechas (Vehicle Service)
    try:
        vehiculo = vehiculo_client.obtener_por_id(dto.vehiculoId)
    except grpc.RpcError as e:
        grpc_error_to_http(e)
        raise

    if not vehiculo.activo:
        raise HTTPException(status_code=400, detail="El vehículo no está activo")

    try:
        disponibles = vehiculo_client.consultar_disponibilidad(
            dto.fechaHoraInicio, dto.fechaHoraFin
        )
    except grpc.RpcError as e:
        grpc_error_to_http(e)
        raise

    if not any(v.id == dto.vehiculoId for v in disponibles):
        raise HTTPException(status_code=400, detail="El vehículo no está disponible en esas fechas")

    # 4) Con las 2 validaciones OK, se crea la reserva (Rental Service)
    try:
        return reserva_client.crear(
            dto.clienteId, dto.vehiculoId,
            dto.fechaHoraInicio, dto.fechaHoraFin,
            vehiculo.precioDiario,
        )
    except grpc.RpcError as e:
        grpc_error_to_http(e)


@app.post("/api/v1/reservas/{id}/cancelar", response_model=ReservaOut)
def cancelar_reserva(id: int):
    try:
        return reserva_client.cancelar(id)
    except grpc.RpcError as e:
        grpc_error_to_http(e)

graphql_app = GraphQLRouter(schema)
app.include_router(graphql_app, prefix="/graphql")