# 🚗 Rentar - Sistema de Alquiler de Vehículos
**Universidad Nacional de Lanús (UNLa)**  
**Materia:** Desarrollo de Software en Sistemas Distribuidos  
**Hito:** Hito 1 (REST / GraphQL)

---

## 📌 Descripción General

**Rentar** es un sistema web para la administración y alquiler de vehículos, correspondiente al **Hito 1** de la materia Desarrollo de Software en Sistemas Distribuidos.
Permite administrar:
- **Gestión de Vehículos (REST):** ABM de vehículos con baja lógica, control de unicidad de patentes y estados (`DISPONIBLE`, `RESERVADO`, `EN_ALQUILER`).
- **Consulta de Disponibilidad (GraphQL):** Búsqueda de vehículos disponibles con fechas obligatorias de inicio/fin y filtros opcionales (tipo, marca, modelo, rango de precio).
- **Gestión de Clientes (REST):** ABM de clientes con validación estricta de documentos y emails únicos, y baja lógica.
- **Alta de Reservas (REST):** Creación de reservas con verificación de estado activo, validación de fechas futuras, prevención de solapamientos y cálculo automático de tarifas.
- **Consulta de Reservas (GraphQL):** Filtrado de reservas para clientes (propias) y administradores (todas).
- **Cancelación de Reservas (REST):** Cancelación lógica previa al inicio del período reservado.
- **Historial de Alquileres (GraphQL):** Consulta histórica de reservas finalizadas y canceladas.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 4.1.1
- **Persistencia:** Spring Data JPA + Hibernate + Base de Datos en Memoria H2
- **APIs y Comunicación:**
  - **REST:** Endpoints documentados interactivamente con **Swagger / OpenAPI 3**.
  - **GraphQL:** Esquemas documentados y testeables desde la consola web **GraphiQL**.
- **Frontend Web:** Interfaz web interactiva basada en HTML5, JavaScript y Bootstrap 5 servida directamente desde Spring Boot.

---

## 🚀 Requisitos Previos

1. **Java JDK 21** instalado y configurado en las variables de entorno (`java -version`).
2. Conexión a Internet en la primera compilación para descargar dependencias vía Maven Wrapper.

*(No es necesario tener Maven instalado por separado, el proyecto incluye el ejecutable `mvnw.cmd` / `./mvnw`).*

---

## 💻 Cómo Levantar el Proyecto

### Paso 1: Abrir la terminal en la raíz del proyecto
```powershell
cd "C:\Users\aggus\OneDrive\Desktop\Sistemas Distribuidos\tp-distribuidos"
```

### Paso 2: Verificar que el puerto 8080 esté libre
Si tienes una instancia previa de Java corriendo, ciérrala antes de iniciar:
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
Started TpDistribuidosApplication in X.XXX seconds
```

---

## 🌐 Cómo Utilizar el Proyecto

Una vez que la aplicación esté corriendo en el puerto `8080`, tienes acceso a los siguientes puntos de entrada:

### 1. Interfaz Web (Frontend)
👉 **URL:** [http://localhost:8080](http://localhost:8080)
- **Consulta de Disponibilidad (GraphQL):** Selecciona fechas y horas de inicio/fin, filtra por tipo de vehículo, marca o modelo, y visualiza los vehículos libres sin solapamientos.
- **Crear Reserva:** Selecciona un vehículo directamente desde la búsqueda o ingresa el ID de cliente y vehículo para confirmar la reserva.
- **Historial y Cancelaciones:** Consulta los alquileres de cualquier cliente y anula reservas antes de su inicio.
- **Panel Admin:** Formularios para dar de alta nuevos vehículos y clientes.

### 2. Documentación Swagger / OpenAPI (REST)
👉 **URL:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Permite probar de forma interactiva todos los endpoints REST:
  - `/api/v1/vehiculos`: `POST`, `PUT`, `DELETE`, `GET /{id}`, `GET`.
  - `/api/v1/clientes`: `POST`, `PUT`, `DELETE`, `GET /{id}`, `GET`.
  - `/api/v1/reservas`: `POST` (alta con validaciones), `POST /{id}/cancelar`.

### 3. Consola GraphiQL (GraphQL)
👉 **URL:** [http://localhost:8080/graphiql](http://localhost:8080/graphiql)
Permite escribir y ejecutar queries GraphQL de forma manual con autocompletado y documentación integrada.

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

---

## 👥 Datos Precargados para Pruebas Rápidas

Al arrancar, el sistema inicializa automáticamente datos de prueba en la base de datos en memoria:
- **Clientes:**
  - ID `1`: Agustín González (DNI: `38450123`, Email: `agustin@unla.edu.ar`)
  - ID `2`: María Fernández (DNI: `40123987`, Email: `maria.fernandez@gmail.com`)
- **Vehículos:**
  - ID `1`: Toyota Corolla (SEDAN, Patente: `AE123CD`, $45.000/día)
  - ID `2`: Ford Ranger (PICKUP, Patente: `AF456GH`, $75.000/día)
  - ID `3`: Volkswagen Taos (SUV, Patente: `AD789JK`, $60.000/día)
  - ID `4`: Peugeot 208 (HATCHBACK, Patente: `AC321MN`, $38.000/día)
