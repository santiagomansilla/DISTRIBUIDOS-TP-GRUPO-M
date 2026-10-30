window.addEventListener('DOMContentLoaded', () => {
    lucide.createIcons();
});

// Requerimiento 2: Disponibilidad de Vehículos (GraphQL)
async function consultarDisponibilidad() {
    const inicio = document.getElementById('disp-inicio').value;
    const fin = document.getElementById('disp-fin').value;

    const query = `
        query ($filtro: DisponibilidadFilter!) {
            consultarDisponibilidad(filtro: $filtro) {
                id patente marca modelo anio color tipo precioDiario
            }
        }
    `;

    const variables = {
        filtro: {
            fechaHoraInicio: inicio ? inicio + ":00" : null,
            fechaHoraFin: fin ? fin + ":00" : null,
            tipo: document.getElementById('disp-tipo').value || null,
            marca: document.getElementById('disp-marca').value || null,
            modelo: document.getElementById('disp-modelo').value || null,
            precioMin: document.getElementById('disp-precio-min').value ? parseFloat(document.getElementById('disp-precio-min').value) : null,
            precioMax: document.getElementById('disp-precio-max').value ? parseFloat(document.getElementById('disp-precio-max').value) : null
        }
    };

    try {
        const resp = await fetch(ENDPOINTS.graphql, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ query, variables })
        });
        const data = await resp.json();
        const tbody = document.getElementById('tabla-disponibles');
        tbody.innerHTML = '';

        if (data.errors) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-danger">${data.errors[0].message}</td></tr>`;
            return;
        }

        const lista = data.data?.consultarDisponibilidad;
        if (!lista || lista.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-secondary">No hay vehículos disponibles con esos filtros.</td></tr>`;
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
                    <td>$${v.precioDiario}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="seleccionarParaReserva(${v.id})">Reservar</button>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        alert('Error al consultar GraphQL: ' + err.message);
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
    const payload = {
        clienteId: parseInt(document.getElementById('res-cliente-id').value),
        vehiculoId: parseInt(document.getElementById('res-vehiculo-id').value),
        fechaHoraInicio: document.getElementById('res-inicio').value + ":00",
        fechaHoraFin: document.getElementById('res-fin').value + ":00"
    };

    const out = document.getElementById('res-resultado');
    out.innerHTML = '';

    try {
        const resp = await fetch(ENDPOINTS.reservas, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });

        if (resp.ok) {
            const res = await resp.json();
            out.innerHTML = `<div class="alert alert-success">¡Reserva #${res.id} confirmada! Total: $${res.importeTotal} | Estado: ${res.estado}</div>`;
        } else {
            const err = await resp.text();
            out.innerHTML = `<div class="alert alert-danger">Error: ${err}</div>`;
        }
    } catch (e) {
        out.innerHTML = `<div class="alert alert-danger">Error de red: ${e.message}</div>`;
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

// Requerimiento 7: Historial de Alquileres (GraphQL)
async function consultarHistorial() {
    const clienteId = document.getElementById('hist-cliente-id').value;
    const query = `
        query ($clienteId: ID!) {
            historialAlquileres(clienteId: $clienteId) {
                id
                vehiculo { marca modelo patente }
                fechaHoraInicio fechaHoraFin cantidadDias importeTotal estado
            }
        }
    `;

    try {
        const resp = await fetch(ENDPOINTS.graphql, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ query, variables: { clienteId } })
        });
        const data = await resp.json();
        const tbody = document.getElementById('tabla-historial');
        tbody.innerHTML = '';

        const list = data.data?.historialAlquileres;
        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center text-secondary">No hay reservas registradas para este cliente.</td></tr>`;
            return;
        }

        list.forEach(r => {
            const badge = r.estado === 'CANCELADA' ? 'bg-danger' : 'bg-success';
            tbody.innerHTML += `
                <tr>
                    <td>#${r.id}</td>
                    <td>${r.vehiculo ? r.vehiculo.marca + ' ' + r.vehiculo.modelo : '-'}</td>
                    <td>${r.vehiculo ? r.vehiculo.patente : '-'}</td>
                    <td>${r.fechaHoraInicio}</td>
                    <td>${r.fechaHoraFin}</td>
                    <td>${r.cantidadDias}</td>
                    <td>$${r.importeTotal}</td>
                    <td><span class="badge ${badge}">${r.estado}</span></td>
                </tr>
            `;
        });
    } catch (e) {
        alert('Error al consultar historial: ' + e.message);
    }
}