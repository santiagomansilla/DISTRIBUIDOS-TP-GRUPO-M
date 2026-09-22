# 🚗 Rentar - Sistema de Alquiler de Vehículos
**Universidad Nacional de Lanús (UNLa)**  
**Materia:** Desarrollo de Software en Sistemas Distribuidos  
**Hitos:** Hito 1 (REST / GraphQL) & Hito 2 (gRPC / Protocol Buffers)

---

## 📌 Descripción General

**Rentar** es un sistema web distribuido e incremental para la gestión y alquiler de flotas de vehículos.
Permite administrar:
- Catálogo de vehículos (ABM con baja lógica y control de estados: `DISPONIBLE`, `RESERVADO`, `EN_ALQUILER`).
- Registro y gestión de clientes (ABM con validación estricta de documentos y emails únicos).
- Reservas de vehículos con cálculo automático de tarifas diarias, validación de fechas futuras y prevención de solapamientos.
- Consultas flexibles mediante **GraphQL** y servicios de alto rendimiento vía **gRPC**.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 4.1.1
- **Persistencia:** Spring Data JPA + Hibernate + Base de Datos en Memoria H2
- **APIs y Comunicación:**
  - **REST:** Endpoints para gestión de flota, clientes y reservas, documentados con **Swagger/OpenAPI 3**.
  - **GraphQL:** Consultas de disponibilidad en rangos de fechas con filtros y consulta de historial de alquileres con **GraphiQL**.
  - **gRPC (Hito 2):** Servidor RPC basado en Protocol Buffers (`.proto`) escuchando en el puerto `9090`.
- **Frontend:** Interfaz web interactiva basada en HTML5, JavaScript y Bootstrap 5 servida directamente desde Spring Boot.

---

## 🚀 Requisitos Previos

1. **Java JDK 21** instalado y configurado en el `PATH` (`java -version`).
2. Conexión a Internet en la primera compilación para descargar dependencias de Maven.

*(No es necesario tener Maven instalado por separado, el proyecto incluye el ejecutable `mvnw.cmd` / `./mvnw`).*

---

## 💻 Cómo Levantar el Proyecto

### Paso 1: Clonar o abrir la carpeta del proyecto
Abre una terminal (PowerShell o CMD) en el directorio raíz del proyecto:
```powershell
cd "C:\Users\aggus\OneDrive\Desktop\Sistemas Distribuidos\tp-distribuidos"
```

### Paso 2: Verificar que el puerto 8080 esté libre
Si tienes una instancia anterior corriendo, ciérrala con `Ctrl + C` o verifica que no haya procesos ocupando el puerto:
```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
```

### Paso 3: Compilar y ejecutar
Ejecuta la aplicación mediante el Maven Wrapper:
```powershell
.\mvnw.cmd spring-boot:run
```

Al finalizar la inicialización, verás en la consola:
```text
Tomcat started on port 8080 (http)
Detected grpc-netty-shaded: Creating ShadedNettyGrpcServerFactory
Started TpDistribuidosApplication in X.XXX seconds
```

---

## 🌐 Cómo Utilizar el Proyecto

Una vez que la aplicación esté corriendo, tienes acceso a los siguientes puntos de entrada:

### 1. Interfaz Web (Frontend de Usuario y Administrador)
👉 **URL:** [http://localhost:8080](http://localhost:8080)
- **Consulta de Disponibilidad (GraphQL):** Selecciona fechas y horas de inicio/fin, filtra por tipo de vehículo, marca o modelo, y visualiza los vehículos libres sin solapamientos.
- **Crear Reserva:** Selecciona un vehículo directamente desde la búsqueda o ingresa el ID de cliente y vehículo para confirmar la reserva.
- **Historial y Cancelaciones:** Consulta los alquileres finalizados y cancelados de cualquier cliente y anula reservas antes de su inicio.
- **Panel Admin:** Formularios para dar de alta nuevos vehículos y clientes.

### 2. Documentación Swagger / OpenAPI (REST)
👉 **URL:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Permite probar de forma interactiva los endpoints REST:
  - `/api/v1/vehiculos`: `POST`, `PUT`, `DELETE`, `GET /{id}`, `GET`.
  - `/api/v1/clientes`: `POST`, `PUT`, `DELETE`, `GET /{id}`, `GET`.
  - `/api/v1/reservas`: `POST` (alta con validaciones), `POST /{id}/cancelar`.

### 3. Consola GraphiQL (GraphQL)
👉 **URL:** [http://localhost:8080/graphiql](http://localhost:8080/graphiql)
Permite escribir y ejecutar queries GraphQL de forma manual.

#### Ejemplo de Query de Disponibilidad:
```graphql
query {
  consultarDisponibilidad(filtro: {
    fechaHoraInicio: "2026-10-01T10:00:00",
    fechaHoraFin: "2026-10-05T18:00:00",
    tipo: "SEDAN"
  }) {
    id
    patente
    marca
    modelo
    precioDiario
  }
}
```

#### Ejemplo de Historial de Alquileres:
```graphql
query {
  historialAlquileres(clienteId: 1) {
    id
    vehiculo {
      marca
      modelo
      patente
    }
    fechaHoraInicio
    fechaHoraFin
    cantidadDias
    importeTotal
    estado
  }
}
```

### 4. Consola de Base de Datos H2
👉 **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** `jdbc:h2:mem:tpdb`
- **User Name:** `sa`
- **Password:** *(dejar vacío)*

### 5. Servicios gRPC (Puerto 9090)
El servidor gRPC está activo en el puerto `9090` con los servicios definidos en `src/main/proto/`:
- `VehiculoGrpcService`
- `ClienteGrpcService`
- `ReservaGrpcService`

Puedes probarlos con herramientas como **Postman** (sección gRPC), **BloomRPC** o **gRPCurl**.

---

## 👥 Datos Precargados para Pruebas Rápidas

Al arrancar, el sistema inicializa automáticamente datos de prueba:
- **Clientes:**
  - ID `1`: Agustín González (DNI: `38450123`, Email: `agustin@unla.edu.ar`)
  - ID `2`: María Fernández (DNI: `40123987`, Email: `maria.fernandez@gmail.com`)
- **Vehículos:**
  - ID `1`: Toyota Corolla (SEDAN, Patente: `AE123CD`, $45.000/día)
  - ID `2`: Ford Ranger (PICKUP, Patente: `AF456GH`, $75.000/día)
  - ID `3`: Volkswagen Taos (SUV, Patente: `AD789JK`, $60.000/día)
  - ID `4`: Peugeot 208 (HATCHBACK, Patente: `AC321MN`, $38.000/día)
