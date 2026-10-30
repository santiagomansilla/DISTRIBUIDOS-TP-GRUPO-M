// URL base de comunicación con el API Gateway en FastAPI
const GATEWAY_URL = 'http://localhost:8000';

const ENDPOINTS = {
    clientes: `${GATEWAY_URL}/api/v1/clientes`,
    vehiculos: `${GATEWAY_URL}/api/v1/vehiculos`,
    reservas: `${GATEWAY_URL}/api/v1/reservas`,
    graphql: `${GATEWAY_URL}/graphql`,
    login: `${GATEWAY_URL}/api/v1/auth/login` // Preparado para Spring Security / JWT
};

// Utilidad para extraer y adjuntar token de autenticación cuando Spring Security esté activo
function getAuthHeaders() {
    const token = localStorage.getItem('rentar_token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}