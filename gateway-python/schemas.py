from pydantic import BaseModel
from typing import Optional
from datetime import datetime

# CLIENTES

class ClienteCreate(BaseModel):
    documento: str
    nombre: str
    apellido: str
    email: str
    telefono: Optional[str] = None
    fechaNacimiento: Optional[str] = None  # formato "YYYY-MM-DD"

class ClienteUpdate(BaseModel):
    nombre: str
    apellido: str
    telefono: Optional[str] = None
    fechaNacimiento: Optional[str] = None

class ClienteOut(BaseModel):
    id: int
    documento: str
    nombre: str
    apellido: str
    email: str
    telefono: str
    fechaNacimiento: str
    activo: bool

# VEHICULOS

class VehiculoCreate(BaseModel):
    patente: str
    marca: str
    modelo: str
    anio: int
    color: Optional[str] = None
    tipo: Optional[str] = None       # "SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"
    precioDiario: float
    estado: Optional[str] = None     # "DISPONIBLE", "RESERVADO", "EN_ALQUILER"

class VehiculoUpdate(BaseModel):
    marca: str
    modelo: str
    anio: int
    color: Optional[str] = None
    tipo: Optional[str] = None
    precioDiario: float
    estado: Optional[str] = None

class VehiculoOut(BaseModel):
    id: int
    patente: str
    marca: str
    modelo: str
    anio: int
    color: str
    tipo: str
    precioDiario: float
    estado: str
    activo: bool


class VehiculoExternoCreate(BaseModel):
    patente: str
    marca: str
    modelo: str
    anio: int
    color: Optional[str] = None
    tipo: Optional[str] = None       
    precioDiario: float
    documentoCliente: int

class IncorporacionExternaOut(BaseModel):
    mensaje: str
    vehiculo: VehiculoOut

# RESERVAS

class ReservaCreate(BaseModel):
    clienteId: int
    vehiculoId: int
    fechaHoraInicio: str  # ISO-8601, ej: "2026-01-15T10:00:00"
    fechaHoraFin: str

class ReservaOut(BaseModel):
    id: int
    clienteId: int
    vehiculoId: int
    fechaHoraInicio: str
    fechaHoraFin: str
    precioDiario: float
    importeTotal: float
    estado: str