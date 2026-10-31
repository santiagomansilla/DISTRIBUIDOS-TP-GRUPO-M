// URL base de comunicación con el API Gateway en FastAPI
var GATEWAY_URL = window.GATEWAY_URL || 'http://localhost:8000';

var ENDPOINTS = window.ENDPOINTS || {
    clientes: `${GATEWAY_URL}/api/v1/clientes`,
    vehiculos: `${GATEWAY_URL}/api/v1/vehiculos`,
    reservas: `${GATEWAY_URL}/api/v1/reservas`,
    graphql: `${GATEWAY_URL}/graphql`,
    login: `${GATEWAY_URL}/api/v1/auth/login`
};

// Utilidad centralizada de autenticación
function getAuthHeaders() {
    const token = localStorage.getItem('rentar_token') || localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}