window.addEventListener('DOMContentLoaded', () => {
    lucide.createIcons();
    cargarFlotaVehiculos();
    listarClientes();
});

// Requerimiento 1: Flota y ABM Vehículos (REST)[cite: 6, 7]
async function cargarFlotaVehiculos() {
    const tbody = document.getElementById('tabla-vehiculos-admin');
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
        patente: document.getElementById('v-patente').value.trim(),
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
            alert(esEdicion ? 'Vehículo modificado correctamente' : 'Vehículo creado');
            cancelarEdicionVehiculo();
            cargarFlotaVehiculos();
        } else {
            alert('Error en la operación: ' + await resp.text());
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

function prepararEdicionVehiculo(v) {
    document.getElementById('v-id').value = v.id;
    document.getElementById('v-patente').value = v.patente;
    document.getElementById('v-patente').disabled = true; // Inmutable según enunciado[cite: 7]
    document.getElementById('v-marca').value = v.marca;
    document.getElementById('v-modelo').value = v.modelo;
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
    document.getElementById('v-estado-container').classList.add('d-none');
    document.getElementById('btn-cancelar-edicion-v').classList.add('d-none');
}

async function toggleActivoVehiculo(id, nuevoEstado) {
    try {
        const resp = await fetch(`${ENDPOINTS.vehiculos}/${id}`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ activo: nuevoEstado })
        });
        if (resp.ok) cargarFlotaVehiculos();
    } catch (err) {
        alert('Error de red: ' + err.message);
    }
}

// Requerimiento 3: ABM Clientes (REST)[cite: 8]
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
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-secondary">Consultando clientes...</td></tr>';
    try {
        const resp = await fetch(ENDPOINTS.clientes, { headers: getAuthHeaders() });
        const datos = await resp.json();
        const clientes = Array.isArray(datos) ? datos : datos?.clientes;
        tbody.innerHTML = '';
        document.getElementById('clientes-total').textContent = clientes.length;

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