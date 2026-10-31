window.addEventListener('DOMContentLoaded', () => {
    if (window.lucide) {
        lucide.createIcons();
    }
    // Inicializa el catálogo de marcas consumiendo el Gateway
    inicializarCatalogosVehiculo();
});

// Requerimiento 2: Disponibilidad de Vehículos (GraphQL)
async function consultarDisponibilidad() {
    const inputInicio = document.getElementById('disp-inicio');
    const inputFin = document.getElementById('disp-fin');
    const tbody = document.getElementById('tabla-disponibles');

    const valorInicio = inputInicio ? inputInicio.value.trim() : "";
    const valorFin = inputFin ? inputFin.value.trim() : "";

    // Validación estricta según Requerimiento 2 (Fechas obligatorias)
    if (!valorInicio || !valorFin) {
        alert("Las fechas y horas de inicio y fin son obligatorias para consultar disponibilidad.");
        return;
    }

    // Asegurar formato ISO-8601 con segundos (YYYY-MM-DDTHH:mm:ss)
    const fechaInicioIso = valorInicio.length === 16 ? `${valorInicio}:00` : valorInicio;
    const fechaFinIso = valorFin.length === 16 ? `${valorFin}:00` : valorFin;

    tbody.innerHTML = '<tr><td colspan="9" class="text-center text-secondary">Consultando flota disponible...</td></tr>';

    const query = `
        query ConsultarDisponibilidad(
            $inicio: String!,
            $fin: String!,
            $tipo: String,
            $marca: String,
            $modelo: String,
            $pMin: Float,
            $pMax: Float
        ) {
            disponibilidad(
                fechaHoraInicio: $inicio,
                fechaHoraFin: $fin,
                tipo: $tipo,
                marca: $marca,
                modelo: $modelo,
                precioMin: $pMin,
                precioMax: $pMax
            ) {
                id
                patente
                marca
                modelo
                anio
                color
                tipo
                precioDiario
            }
        }
    `;

    // Filtros opcionales: si no se seleccionaron, enviar null
    const tipoVal = document.getElementById('disp-tipo')?.value || null;
    const marcaVal = document.getElementById('disp-marca')?.value || null;
    const modeloVal = document.getElementById('disp-modelo')?.value || null;
    
    const pMinRaw = document.getElementById('disp-precio-min')?.value;
    const pMaxRaw = document.getElementById('disp-precio-max')?.value;
    const pMinVal = pMinRaw && !isNaN(pMinRaw) ? parseFloat(pMinRaw) : null;
    const pMaxVal = pMaxRaw && !isNaN(pMaxRaw) ? parseFloat(pMaxRaw) : null;

    const variables = {
        inicio: fechaInicioIso,
        fin: fechaFinIso,
        tipo: tipoVal,
        marca: marcaVal,
        modelo: modeloVal,
        pMin: pMinVal,
        pMax: pMaxVal
    };

    console.log("[GraphQL Request] Variables enviadas:", variables);

    try {
        const resp = await fetch(ENDPOINTS.graphql, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ query, variables })
        });

        const data = await resp.json();
        tbody.innerHTML = '';

        if (data.errors && data.errors.length > 0) {
            console.error("[GraphQL Error]", data.errors);
            tbody.innerHTML = `<tr><td colspan="9" class="text-danger text-center">${data.errors[0].message}</td></tr>`;
            return;
        }

        const lista = data.data?.disponibilidad;
        if (!lista || lista.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-secondary">No hay vehículos disponibles para ese rango o filtros seleccionados.</td></tr>`;
            return;
        }

        lista.forEach(v => {
            tbody.innerHTML += `
                <tr>
                    <td>${v.id}</td>
                    <td><strong>${v.patente}</strong></td>
                    <td>${v.marca}</td>
                    <td>${v.modelo}</td>
                    <td>${v.anio}</td>
                    <td>${v.color || '-'}</td>
                    <td><span class="badge bg-secondary">${v.tipo}</span></td>
                    <td>$${v.precioDiario.toFixed(2)}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="seleccionarParaReserva(${v.id})">Reservar</button>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-danger text-center">Error al conectar con el Gateway: ${err.message}</td></tr>`;
    }
}

function seleccionarParaReserva(id) {
    document.getElementById('res-vehiculo-id').value = id;
    document.getElementById('res-inicio').value = document.getElementById('disp-inicio').value;
    document.getElementById('res-fin').value = document.getElementById('disp-fin').value;
    const triggerEl = document.querySelector('#tab-res-btn');
    if (triggerEl) {
        bootstrap.Tab.getInstance(triggerEl) ? bootstrap.Tab.getInstance(triggerEl).show() : new bootstrap.Tab(triggerEl).show();
    }
}

// Requerimiento 4: Alta de Reserva (REST)
async function crearReserva() {
    const inputCliente = document.getElementById('res-cliente-id');
    const inputVehiculo = document.getElementById('res-vehiculo-id');
    const inputInicio = document.getElementById('res-inicio');
    const inputFin = document.getElementById('res-fin');
    const out = document.getElementById('res-resultado');

    // 1. Obtener clienteId (del input o del login guardado en localStorage)
    let clienteId = parseInt(inputCliente?.value);
    if (isNaN(clienteId) || clienteId <= 0) {
        clienteId = parseInt(localStorage.getItem('clienteId') || sessionStorage.getItem('clienteId'));
    }

    const vehiculoId = parseInt(inputVehiculo?.value);
    const inicioVal = inputInicio?.value?.trim();
    const finVal = inputFin?.value?.trim();

    // 2. Validaciones previas en el cliente
    if (isNaN(clienteId) || clienteId <= 0) {
        out.innerHTML = `<div class="alert alert-danger">Error: No se encontró un ID de cliente válido. Inicie sesión nuevamente.</div>`;
        return;
    }

    if (isNaN(vehiculoId) || vehiculoId <= 0) {
        out.innerHTML = `<div class="alert alert-danger">Error: Debe seleccionar un vehículo válido.</div>`;
        return;
    }

    if (!inicioVal || !finVal) {
        out.innerHTML = `<div class="alert alert-danger">Error: Fechas y horas de inicio y fin requeridas.</div>`;
        return;
    }

    // Formatear fechas a ISO-8601 con segundos
    const fechaHoraInicio = inicioVal.length === 16 ? `${inicioVal}:00` : inicioVal;
    const fechaHoraFin = finVal.length === 16 ? `${finVal}:00` : finVal;

    const payload = {
        clienteId: clienteId,
        vehiculoId: vehiculoId,
        fechaHoraInicio: fechaHoraInicio,
        fechaHoraFin: fechaHoraFin
    };

    console.log("[POST /api/v1/reservas] Enviando payload:", payload);
    out.innerHTML = `<div class="alert alert-info">Procesando reserva...</div>`;

    try {
        const resp = await fetch(ENDPOINTS.reservas, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeaders()
            },
            body: JSON.stringify(payload)
        });

        if (resp.ok) {
            const res = await resp.json();
            out.innerHTML = `
                <div class="alert alert-success">
                    <strong>¡Reserva #${res.id} confirmada!</strong><br>
                    Total a pagar: <strong>$${res.importeTotal}</strong> | Estado: <span class="badge bg-success">${res.estado}</span>
                </div>
            `;
        } else {
            const errData = await resp.json().catch(() => null);
            const errMsg = errData?.detail ? JSON.stringify(errData.detail) : await resp.text();
            out.innerHTML = `<div class="alert alert-danger">Fallo al crear reserva: ${errMsg}</div>`;
        }
    } catch (e) {
        out.innerHTML = `<div class="alert alert-danger">Error de red/comunicación: ${e.message}</div>`;
    }
}

// Requerimiento 6: Cancelación de Reserva (REST)
async function cancelarReserva() {
    const id = document.getElementById('cancelar-reserva-id').value;
    const out = document.getElementById('cancelar-resultado');
    try {
        const resp = await fetch(`${ENDPOINTS.reservas}/${id}/cancelar`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (resp.ok) {
            const res = await resp.json();
            out.innerHTML = `<div class="alert alert-warning">Reserva #${res.id} cancelada. Estado: ${res.estado}</div>`;
        } else {
            const err = await resp.text();
            out.innerHTML = `<div class="alert alert-danger">Error: ${err}</div>`;
        }
    } catch (e) {
        out.innerHTML = `<div class="alert alert-danger">Error: ${e.message}</div>`;
    }
}


/* ==========================================================================
   CATÁLOGOS DINÁMICOS DE MARCAS Y MODELOS (API GATEWAY)
   ========================================================================== */

async function inicializarCatalogosVehiculo() {
    const selectMarca = document.getElementById("disp-marca");
    if (!selectMarca) return;

    selectMarca.innerHTML = '<option value="">Cargando marcas...</option>';

    try {
        const res = await fetch(`${GATEWAY_URL}/api/v1/vehiculos/marcas`);
        if (!res.ok) throw new Error("No se pudo obtener el catálogo de marcas");
        const marcas = await res.json();

        selectMarca.innerHTML = '<option value="">Todas las marcas</option>';
        marcas.forEach(marca => {
            const opt = document.createElement("option");
            opt.value = marca;
            opt.textContent = marca;
            selectMarca.appendChild(opt);
        });
    } catch (err) {
        console.error("Error al cargar marcas desde el Gateway:", err);
        selectMarca.innerHTML = '<option value="">Todas las marcas (No disponible)</option>';
    }
}

async function cargarModelosPorMarca() {
    const marca = document.getElementById("disp-marca").value.trim();
    const selectModelo = document.getElementById("disp-modelo");
    if (!selectModelo) return;

    // Si deselecciona la marca, se restablece el selector de modelo
    if (!marca) {
        selectModelo.innerHTML = '<option value="">Todos los modelos</option>';
        selectModelo.disabled = true;
        return;
    }

    selectModelo.innerHTML = '<option value="">Cargando modelos...</option>';
    selectModelo.disabled = true;

    try {
        const res = await fetch(`${GATEWAY_URL}/api/v1/vehiculos/modelos?marca=${encodeURIComponent(marca)}`);
        if (!res.ok) throw new Error("No se pudo obtener los modelos");
        const modelos = await res.json();

        selectModelo.innerHTML = '<option value="">Todos los modelos</option>';
        if (modelos && modelos.length > 0) {
            modelos.forEach(mod => {
                const opt = document.createElement("option");
                opt.value = mod;
                opt.textContent = mod;
                selectModelo.appendChild(opt);
            });
            selectModelo.disabled = false;
        } else {
            selectModelo.innerHTML = '<option value="">Sin modelos registrados</option>';
            selectModelo.disabled = false;
        }
    } catch (err) {
        console.error("Error al cargar modelos desde el Gateway:", err);
        selectModelo.innerHTML = '<option value="">Todos los modelos</option>';
        selectModelo.disabled = false;
    }
}
function obtenerClienteIdSesion() {
    return parseInt(localStorage.getItem('clienteId') || sessionStorage.getItem('clienteId') || 2);
}

// Cargar automáticamente al cambiar a la pestaña de historial
document.getElementById('tab-hist-btn')?.addEventListener('click', () => {
    consultarHistorial();
});


/* ==========================================================================
   HITO 3: INCORPORACIÓN DE VEHÍCULO EXTERNO (SCATTER-GATHER CON RABBITMQ)
   ========================================================================== */

function obtenerDocumentoClienteSesion() {
    return parseInt(
        localStorage.getItem('documento') || 
        sessionStorage.getItem('documento') || 
        localStorage.getItem('clienteDocumento') || 
        30123456
    );
}

// 1. Manejador para el formulario de la pestaña "Ofrecer mi Vehículo"
async function enviarSolicitudExterna(e) {
    if (e && typeof e.preventDefault === 'function') {
        e.preventDefault();
    }

    const val = (id) => {
        const el = document.getElementById(id);
        return el ? el.value.trim() : "";
    };

    const patente = val('ext-tab-patente').toUpperCase();
    const marca = val('ext-tab-marca');
    const modelo = val('ext-tab-modelo');
    const anio = parseInt(val('ext-tab-anio')) || new Date().getFullYear();
    const color = val('ext-tab-color') || "-";
    const tipo = val('ext-tab-tipo') || "SEDAN";
    const precioDiario = parseFloat(val('ext-tab-precio')) || 0.0;
    
    // Si no cargó el DNI en el input, busca el de la sesión
    let documentoCliente = parseInt(val('ext-tab-dni'));
    if (isNaN(documentoCliente) || documentoCliente <= 0) {
        documentoCliente = obtenerDocumentoClienteSesion();
    }

    if (!patente || !marca || !modelo || !documentoCliente) {
        alert("Por favor complete los campos obligatorios: Patente, Marca, Modelo y DNI Titular.");
        return;
    }

    const payload = {
        patente,
        marca,
        modelo,
        anio,
        color,
        tipo,
        precioDiario,
        documentoCliente
    };

    console.log("[Hito 3] Despachando solicitud externa desde tab:", payload);
    await despacharIncorporacionExterna(payload, 'ext-tab-resultado', 'form-incorporar-tab');
}

// 2. Manejador para el formulario del Modal
async function enviarSolicitudModal(e) {
    if (e && typeof e.preventDefault === 'function') {
        e.preventDefault();
    }

    const val = (id) => {
        const el = document.getElementById(id);
        return el ? el.value.trim() : "";
    };

    const patente = val('m-ext-patente').toUpperCase();
    const marca = val('m-ext-marca');
    const modelo = val('m-ext-modelo');
    const anio = parseInt(val('m-ext-anio')) || new Date().getFullYear();
    const color = val('m-ext-color') || "-";
    const tipo = val('m-ext-tipo') || "SEDAN";
    const precioDiario = parseFloat(val('m-ext-precio')) || 0.0;
    
    let documentoCliente = parseInt(val('m-ext-dni'));
    if (isNaN(documentoCliente) || documentoCliente <= 0) {
        documentoCliente = obtenerDocumentoClienteSesion();
    }

    if (!patente || !marca || !modelo || !documentoCliente) {
        alert("Por favor complete los campos obligatorios: Patente, Marca, Modelo y DNI Titular.");
        return;
    }

    const payload = {
        patente,
        marca,
        modelo,
        anio,
        color,
        tipo,
        precioDiario,
        documentoCliente
    };

    console.log("[Hito 3] Despachando solicitud externa desde modal:", payload);
    const exito = await despacharIncorporacionExterna(payload, null, 'form-alta-modal');
    
    if (exito) {
        const modalEl = document.getElementById('modalAltaVehiculoCliente');
        if (modalEl && window.bootstrap) {
            const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
            modal.hide();
        }
    }
}

// 3. Función centralizada de despacho al Gateway
async function despacharIncorporacionExterna(payload, contenedorResultadoId, formId) {
    const contenedor = contenedorResultadoId ? document.getElementById(contenedorResultadoId) : null;
    const urlGateway = (typeof GATEWAY_URL !== 'undefined' ? GATEWAY_URL : 'http://localhost:8000').replace(/\/+$/, '');

    if (contenedor) {
        contenedor.innerHTML = `<div class="alert alert-info">Enviando solicitud asíncrona al API Gateway...</div>`;
    }

    try {
        const resp = await fetch(`${urlGateway}/api/v1/vehiculos/externos`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(typeof getAuthHeaders === 'function' ? getAuthHeaders() : {})
            },
            body: JSON.stringify(payload)
        });

        if (resp.status === 202 || resp.ok) {
            const data = await resp.json();
            const msg = `¡Solicitud registrada con éxito! El vehículo ${data.patente} ingresó con ID ${data.id} inactivo. Se iniciaron las validaciones concurrentes con Policía y DNRPA vía RabbitMQ.`;

            if (contenedor) {
                contenedor.innerHTML = `<div class="alert alert-success">${msg}</div>`;
            } else {
                alert(msg);
            }

            const form = document.getElementById(formId);
            if (form) form.reset();
            return true;
        } else {
            const err = await resp.json().catch(() => null);
            const errMsg = 'Error al solicitar incorporación: ' + (err?.detail || await resp.text());
            if (contenedor) {
                contenedor.innerHTML = `<div class="alert alert-danger">${errMsg}</div>`;
            } else {
                alert(errMsg);
            }
            return false;
        }
    } catch (err) {
        const netMsg = 'Error de conexión con el API Gateway: ' + err.message;
        if (contenedor) {
            contenedor.innerHTML = `<div class="alert alert-danger">${netMsg}</div>`;
        } else {
            alert(netMsg);
        }
        return false;
    }
}




// Requerimiento 7: Historial de Alquileres (GraphQL)
async function consultarHistorial() {
    const tbody = document.getElementById('tabla-historial');
    if (!tbody) return;

    // Obtiene el clienteId guardado durante el login
    const clienteId = parseInt(localStorage.getItem('clienteId') || sessionStorage.getItem('clienteId') || 2);

    tbody.innerHTML = '<tr><td colspan="8" class="text-center text-secondary">Consultando historial en el Core...</td></tr>';

    const query = `
        query ObtenerHistorial($clienteId: Int!) {
            historial(clienteId: $clienteId) {
                id
                vehiculoMarca
                vehiculoModelo
                patente
                fechaHoraInicio
                fechaHoraFin
                cantidadDias
                importeTotal
                estado
            }
        }
    `;

    try {
        const resp = await fetch(ENDPOINTS.graphql, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeaders()
            },
            body: JSON.stringify({
                query: query,
                variables: { clienteId: clienteId }
            })
        });

        const data = await resp.json();
        tbody.innerHTML = '';

        if (data.errors && data.errors.length > 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-danger text-center">${data.errors[0].message}</td></tr>`;
            return;
        }

        const list = data.data?.historial;
        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-secondary">No registra alquileres o reservas pasadas.</td></tr>`;
            return;
        }

        list.forEach(r => {
            const badge = r.estado === 'CANCELADA' ? 'bg-danger' : (r.estado === 'CONFIRMADA' ? 'bg-success' : 'bg-primary');
            const fInicio = r.fechaHoraInicio ? r.fechaHoraInicio.replace('T', ' ').substring(0, 16) : '-';
            const fFin = r.fechaHoraFin ? r.fechaHoraFin.replace('T', ' ').substring(0, 16) : '-';

            tbody.innerHTML += `
                <tr>
                    <td><strong>#${r.id}</strong></td>
                    <td>${r.vehiculoMarca} ${r.vehiculoModelo}</td>
                    <td><span class="badge bg-dark">${r.patente}</span></td>
                    <td>${fInicio}</td>
                    <td>${fFin}</td>
                    <td>${r.cantidadDias}</td>
                    <td><strong>$${r.importeTotal.toFixed(2)}</strong></td>
                    <td><span class="badge ${badge}">${r.estado}</span></td>
                </tr>
            `;
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-danger text-center">Error de red: ${e.message}</td></tr>`;
    }
}