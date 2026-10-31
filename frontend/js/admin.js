window.addEventListener('DOMContentLoaded', () => {
    if (window.lucide) {
        lucide.createIcons();
    }
    inicializarCatalogosVehiculo();
    cargarFlotaVehiculos();
    listarClientes();
});

/* ==========================================================================
   CATÁLOGOS DINÁMICOS (MARCAS, MODELOS Y AÑOS)
   ========================================================================== */

async function inicializarCatalogosVehiculo() {
    const selectMarca = document.getElementById("v-marca");
    const selectAnio = document.getElementById("v-anio");

    // Llenar select de años (año corriente hasta 20 años hacia atrás)
    if (selectAnio) {
        selectAnio.innerHTML = '<option value="" disabled selected>Seleccione año...</option>';
        const anioActual = new Date().getFullYear();
        for (let anio = anioActual; anio >= anioActual - 20; anio--) {
            const opt = document.createElement("option");
            opt.value = anio;
            opt.textContent = anio;
            selectAnio.appendChild(opt);
        }
    }

    // Consultar marcas al API Gateway
    if (selectMarca) {
        try {
            const res = await fetch(`${GATEWAY_URL}/api/v1/vehiculos/marcas`);
            if (!res.ok) throw new Error("No se pudo obtener el catálogo");
            const marcas = await res.json();

            selectMarca.innerHTML = '<option value="" disabled selected>Seleccione marca...</option>';
            marcas.forEach(marca => {
                const opt = document.createElement("option");
                opt.value = marca;
                opt.textContent = marca;
                selectMarca.appendChild(opt);
            });
        } catch (err) {
            console.error("Error al cargar marcas:", err);
            selectMarca.innerHTML = '<option value="" disabled selected>Error al cargar marcas</option>';
        }
    }
}

async function cargarModelosPorMarca(modeloPreseleccionado = null) {
    const marca = document.getElementById("v-marca").value;
    const selectModelo = document.getElementById("v-modelo");

    if (!marca) return;

    selectModelo.innerHTML = '<option value="" disabled selected>Cargando modelos...</option>';
    selectModelo.disabled = true;

    try {
        const res = await fetch(`${GATEWAY_URL}/api/v1/vehiculos/modelos?marca=${encodeURIComponent(marca)}`);
        const modelos = await res.json();

        selectModelo.innerHTML = '<option value="" disabled selected>Seleccione modelo...</option>';
        if (modelos && modelos.length > 0) {
            modelos.forEach(mod => {
                const opt = document.createElement("option");
                opt.value = mod;
                opt.textContent = mod;
                if (modeloPreseleccionado && mod.toLowerCase() === modeloPreseleccionado.toLowerCase()) {
                    opt.selected = true;
                }
                selectModelo.appendChild(opt);
            });
            selectModelo.disabled = false;
        } else {
            selectModelo.innerHTML = '<option value="" disabled selected>Sin modelos registrados</option>';
        }
    } catch (err) {
        console.error("Error al cargar modelos:", err);
        selectModelo.innerHTML = '<option value="" disabled selected>Error al obtener modelos</option>';
    }
}

/* ==========================================================================
   REQUERIMIENTO 1: FLOTA Y ABM VEHÍCULOS (REST - ADMINISTRADOR)
   ========================================================================== */

async function cargarFlotaVehiculos() {
    const tbody = document.getElementById('tabla-vehiculos-admin');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="10" class="text-center text-secondary">Actualizando flota...</td></tr>';

    try {
        const resp = await fetch(ENDPOINTS.vehiculos, { headers: getAuthHeaders() });
        const lista = await resp.json();
        tbody.innerHTML = '';

        if (!lista || lista.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" class="text-center text-secondary">No hay vehículos registrados.</td></tr>';
            return;
        }

        lista.forEach(v => {
            const badgeEstado = v.estado === 'DISPONIBLE' ? 'bg-success' : (v.estado === 'RESERVADO' ? 'bg-warning text-dark' : 'bg-primary');
            tbody.innerHTML += `
                <tr>
                    <td>${v.id}</td>
                    <td><strong>${v.patente}</strong></td>
                    <td>${v.marca} ${v.modelo}</td>
                    <td>${v.anio}</td>
                    <td>${v.color || '-'}</td>
                    <td><span class="badge bg-secondary">${v.tipo}</span></td>
                    <td>$${v.precioDiario}</td>
                    <td><span class="badge ${badgeEstado}">${v.estado}</span></td>
                    <td><span class="badge ${v.activo ? 'bg-success' : 'bg-danger'}">${v.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                        <div class="d-flex gap-1">
                            <button class="btn btn-sm btn-outline-info py-0 px-2" onclick="prepararEdicionVehiculo(${JSON.stringify(v).replace(/"/g, '&quot;')})">Editar</button>
                            <button class="btn btn-sm ${v.activo ? 'btn-outline-danger' : 'btn-outline-success'} py-0 px-2" onclick="toggleActivoVehiculo(${v.id}, ${!v.activo})">${v.activo ? 'Baja' : 'Activar'}</button>
                        </div>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="10" class="text-danger">Error: ${err.message}</td></tr>`;
    }
}

async function guardarVehiculo(e) {
    e.preventDefault();
    const id = document.getElementById('v-id').value;
    const esEdicion = Boolean(id);

    const payload = {
        patente: document.getElementById('v-patente').value.trim().toUpperCase(),
        marca: document.getElementById('v-marca').value.trim(),
        modelo: document.getElementById('v-modelo').value.trim(),
        anio: parseInt(document.getElementById('v-anio').value),
        color: document.getElementById('v-color').value.trim(),
        tipo: document.getElementById('v-tipo').value,
        precioDiario: parseFloat(document.getElementById('v-precio').value),
        estado: esEdicion ? document.getElementById('v-estado').value : 'DISPONIBLE'
    };

    const url = esEdicion ? `${ENDPOINTS.vehiculos}/${id}` : ENDPOINTS.vehiculos;
    const method = esEdicion ? 'PUT' : 'POST';

    try {
        const resp = await fetch(url, {
            method: method,
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });

        if (resp.ok) {
            alert(esEdicion ? 'Vehículo modificado correctamente' : 'Vehículo registrado en estado DISPONIBLE');
            cancelarEdicionVehiculo();
            cargarFlotaVehiculos();
        } else {
            const errData = await resp.json().catch(() => null);
            alert('Error en la operación: ' + (errData?.detail || await resp.text()));
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function prepararEdicionVehiculo(v) {
    document.getElementById('v-id').value = v.id;
    document.getElementById('v-patente').value = v.patente;
    document.getElementById('v-patente').disabled = true; // Inmutable según consigna
    document.getElementById('v-marca').value = v.marca;

    await cargarModelosPorMarca(v.modelo);

    document.getElementById('v-anio').value = v.anio;
    document.getElementById('v-color').value = v.color || '';
    document.getElementById('v-tipo').value = v.tipo;
    document.getElementById('v-precio').value = v.precioDiario;
    document.getElementById('v-estado').value = v.estado;
    document.getElementById('v-estado-container').classList.remove('d-none');
    document.getElementById('btn-cancelar-edicion-v').classList.remove('d-none');
}

function cancelarEdicionVehiculo() {
    document.getElementById('form-vehiculo').reset();
    document.getElementById('v-id').value = '';
    document.getElementById('v-patente').disabled = false;
    document.getElementById('v-modelo').disabled = true;
    document.getElementById('v-modelo').innerHTML = '<option value="" disabled selected>Primero elija una marca...</option>';
    document.getElementById('v-estado-container').classList.add('d-none');
    document.getElementById('btn-cancelar-edicion-v').classList.add('d-none');
}

async function toggleActivoVehiculo(id, nuevoEstado) {
    try {
        const urlBase = (ENDPOINTS.vehiculos || '').replace(/\/+$/, '');
        const resp = await fetch(`${urlBase}/${id}`, {
            method: 'PATCH',
            headers: {
                ...getAuthHeaders(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ activo: nuevoEstado })
        });

        if (resp.ok) {
            await resp.json(); // Consume el JSON 200 OK
            cargarFlotaVehiculos(); // Recarga la grilla
        } else {
            const errData = await resp.json().catch(() => null);
            alert('No se pudo modificar el estado del vehículo: ' + (errData?.detail || resp.statusText));
        }
    } catch (err) {
        alert('Error de red: ' + err.message);
    }
}

/* ==========================================================================
   REQUERIMIENTO 3: ABM CLIENTES (REST - ADMINISTRADOR)
   ========================================================================== */

async function guardarCliente(e) {
    e.preventDefault();
    const payload = {
        documento: document.getElementById('c-doc').value.trim(),
        nombre: document.getElementById('c-nombre').value.trim(),
        apellido: document.getElementById('c-apellido').value.trim(),
        email: document.getElementById('c-email').value.trim(),
        telefono: document.getElementById('c-tel').value.trim() || null,
        fechaNacimiento: document.getElementById('c-fecha').value || null
    };

    try {
        const resp = await fetch(ENDPOINTS.clientes, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });
        if (resp.ok) {
            alert('Cliente registrado correctamente');
            document.getElementById('form-cliente').reset();
            listarClientes();
        } else {
            alert('Error: ' + await resp.text());
        }
    } catch (err) {
        alert('Error de red: ' + err.message);
    }
}

async function listarClientes() {
    const tbody = document.getElementById('tabla-clientes-grpc');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-secondary">Consultando clientes...</td></tr>';
    try {
        const resp = await fetch(ENDPOINTS.clientes, { headers: getAuthHeaders() });
        const datos = await resp.json();
        const clientes = Array.isArray(datos) ? datos : datos?.clientes || [];
        tbody.innerHTML = '';
        const totalElem = document.getElementById('clientes-total');
        if (totalElem) totalElem.textContent = clientes.length;

        clientes.forEach(cliente => {
            tbody.innerHTML += `
                <tr>
                    <td>${cliente.id}</td>
                    <td>${cliente.documento ?? '-'}</td>
                    <td>${cliente.nombre ?? '-'}</td>
                    <td>${cliente.apellido ?? '-'}</td>
                    <td>${cliente.email ?? '-'}</td>
                    <td><span class="badge ${cliente.activo ? 'bg-success' : 'bg-secondary'}">${cliente.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td><button class="btn btn-sm btn-outline-primary py-0" onclick="seleccionarClienteAdmin(${cliente.id})">Inspeccionar</button></td>
                </tr>
            `;
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger">Error: ${err.message}</td></tr>`;
    }
}

function seleccionarClienteAdmin(id) {
    document.getElementById('grpc-cliente-id').value = id;
    document.getElementById('grpc-validacion-id').value = id;
    consultarCliente();
}

async function consultarCliente() {
    const id = document.getElementById('grpc-cliente-id').value.trim();
    if (!id) return;
    try {
        const resp = await fetch(`${ENDPOINTS.clientes}/${id}`, { headers: getAuthHeaders() });
        const c = await resp.json();
        document.getElementById('grpc-cliente-detalle').innerHTML = `
            <strong class="text-white">${c.nombre} ${c.apellido}</strong> (ID: ${c.id})<br>
            DNI: ${c.documento} | Email: ${c.email} | Estado: <span class="badge ${c.activo ? 'bg-success' : 'bg-danger'}">${c.activo ? 'Activo' : 'Inactivo'}</span>
        `;
    } catch (err) {
        document.getElementById('grpc-cliente-detalle').textContent = 'Error al consultar cliente.';
    }
}

async function verificarCliente(tipo) {
    const id = document.getElementById('grpc-validacion-id').value.trim();
    if (!id) return;
    try {
        const resp = await fetch(`${ENDPOINTS.clientes}/${id}/${tipo}`, { headers: getAuthHeaders() });
        const data = await resp.json();
        const valor = typeof data === 'boolean' ? data : data?.resultado ?? data?.activo ?? data?.existe;
        document.getElementById('grpc-cliente-resultado').innerHTML = `<div class="alert alert-${valor ? 'success' : 'warning'}">Resultado (${tipo}): ${valor ? 'POSITIVO' : 'NEGATIVO'}</div>`;
    } catch (err) {
        document.getElementById('grpc-cliente-resultado').innerHTML = `<div class="alert alert-danger">Error: ${err.message}</div>`;
    }
}

async function validarCliente() {
    const id = document.getElementById('grpc-validacion-id').value.trim();
    if (!id) return;
    try {
        const resp = await fetch(`${ENDPOINTS.clientes}/validar`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ clienteId: Number(id) })
        });
        const res = await resp.json();
        const valido = res?.valido === true;
        document.getElementById('grpc-cliente-resultado').innerHTML = `<div class="alert alert-${valido ? 'success' : 'danger'}">${res?.mensaje || (valido ? 'Cliente habilitado' : 'Cliente bloqueado')}</div>`;
    } catch (err) {
        document.getElementById('grpc-cliente-resultado').innerHTML = `<div class="alert alert-danger">Error: ${err.message}</div>`;
    }
}


/* ==========================================================================
   REQUERIMIENTO 5: CONSULTA DE RESERVAS (GRAPHQL - ADMINISTRADOR)
   ========================================================================== */

async function cargarReservasAdmin() {
    const tbody = document.getElementById('tabla-reservas-admin');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" class="text-center text-secondary">Consultando reservas vía GraphQL...</td></tr>';

    const estado = document.getElementById('filtro-reserva-estado')?.value || null;
    const tipo = document.getElementById('filtro-reserva-tipo')?.value || null;
    const desde = document.getElementById('filtro-reserva-desde')?.value || null;
    const hasta = document.getElementById('filtro-reserva-hasta')?.value || null;

    // Documentación de la query según el schema Strawberry en gateway-python
    const graphqlQuery = {
        query: `
            query ConsultarReservasAdmin($esAdmin: Boolean!, $estado: String, $tipoVehiculo: String, $fechaDesde: String, $fechaHasta: String) {
                reservas(
                    esAdmin: $esAdmin,
                    estado: $estado,
                    tipoVehiculo: $tipoVehiculo,
                    fechaDesde: $fechaDesde,
                    fechaHasta: $fechaHasta
                ) {
                    clienteNombre
                    clienteApellido
                    vehiculoMarca
                    vehiculoModelo
                    patente
                    fechaHoraInicio
                    fechaHoraFin
                    precioDiario
                    importeTotal
                    estado
                }
            }
        `,
        variables: {
            esAdmin: true,
            estado: estado || null,
            tipoVehiculo: tipo || null,
            fechaDesde: desde || null,
            fechaHasta: hasta || null
        }
    };

    try {
        const resp = await fetch(ENDPOINTS.graphql, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(graphqlQuery)
        });

        const json = await resp.json();

        if (json.errors && json.errors.length > 0) {
            throw new Error(json.errors[0].message);
        }

        const lista = json.data?.reservas || [];
        tbody.innerHTML = '';

        if (lista.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-secondary">No se encontraron reservas registradas.</td></tr>';
            return;
        }

        lista.forEach(r => {
            const badgeClase = r.estado === 'CONFIRMADA' ? 'bg-success' : (r.estado === 'CANCELADA' ? 'bg-danger' : 'bg-secondary');
            
            // Formateo legible de fechas ISO
            const fInicio = r.fechaHoraInicio ? r.fechaHoraInicio.replace('T', ' ').substring(0, 16) : '-';
            const fFin = r.fechaHoraFin ? r.fechaHoraFin.replace('T', ' ').substring(0, 16) : '-';

            tbody.innerHTML += `
                <tr>
                    <td><strong>${r.clienteNombre} ${r.clienteApellido}</strong></td>
                    <td>${r.vehiculoMarca} ${r.vehiculoModelo}</td>
                    <td><span class="badge bg-dark">${r.patente}</span></td>
                    <td>${fInicio}</td>
                    <td>${fFin}</td>
                    <td>$${r.precioDiario.toFixed(2)}</td>
                    <td><strong class="text-white">$${r.importeTotal.toFixed(2)}</strong></td>
                    <td><span class="badge ${badgeClase}">${r.estado}</span></td>
                </tr>
            `;
        });

        if (window.lucide) lucide.createIcons();
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-danger text-center">Error al consultar reservas por GraphQL: ${err.message}</td></tr>`;
    }
}