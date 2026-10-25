package com.unla.tp_distribuidos.service;

import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERVICIO AGREGADOR (SCATTER-GATHER)
 * Coordina en memoria las respuestas asíncronas de la Policía y el DNRPA
 * para la incorporación de vehículos externos.
 */
@Service
public class ValidacionDnrpaPolicialService {

    private final VehiculoRepository vehiculoRepository;
    
    // Tabla hash concurrente para almacenar el estado parcial indexado por vehiculoId
    private final Map<Long, EstadoValidacion> agregaciones = new ConcurrentHashMap<>();

    public ValidacionDnrpaPolicialService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    /**
     * Registra el resultado del informe policial.
     * @param vehiculoId Identificador del vehículo
     * @param noRobado true si está limpio, false si posee denuncia de robo
     */
    public synchronized void registrarResultadoPolicia(Long vehiculoId, boolean noRobado) {
        EstadoValidacion estado = agregaciones.computeIfAbsent(vehiculoId, id -> new EstadoValidacion());
        estado.policiaListo = true;
        estado.policiaOk = noRobado;
        evaluarResolucion(vehiculoId, estado);
    }

    /**
     * Registra el resultado de la verificación registral del DNRPA.
     * @param vehiculoId Identificador del vehículo
     * @param esTitular true si el cliente solicitante coincide con el titular registral
     */
    public synchronized void registrarResultadoTitularidad(Long vehiculoId, boolean esTitular) {
        EstadoValidacion estado = agregaciones.computeIfAbsent(vehiculoId, id -> new EstadoValidacion());
        estado.dnrpaListo = true;
        estado.dnrpaOk = esTitular;
        evaluarResolucion(vehiculoId, estado);
    }

    /**
     * Evalúa si ambas verificaciones asíncronas concluyeron y define la persistencia en MariaDB.
     */
    private void evaluarResolucion(Long vehiculoId, EstadoValidacion estado) {
        // Solo ejecuta la resolución cuando AMBOS organismos contestaron
        if (estado.policiaListo && estado.dnrpaListo) {
            Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId).orElse(null);
            
            if (vehiculo != null) {
                if (estado.policiaOk && estado.dnrpaOk) {
                    // Ambos dieron el visto bueno -> se habilita operativamente
                    vehiculo.setActivo(true);
                    vehiculoRepository.save(vehiculo);
                    System.out.println("=================================================================");
                    System.out.println("✅ [ALTA EXTERNA APROBADA] Vehículo ID " + vehiculoId 
                            + " (" + vehiculo.getPatente() + ") superó Policía y DNRPA. Habilitado (activo = true).");
                    System.out.println("=================================================================");
                } else {
                    // Al menos uno falló -> permanece inhabilitado lógicamente
                    vehiculo.setActivo(false);
                    vehiculoRepository.save(vehiculo);
                    System.out.println("=================================================================");
                    System.out.println("❌ [ALTA EXTERNA RECHAZADA] Vehículo ID " + vehiculoId + " no cumple requisitos.");
                    if (!estado.policiaOk) {
                        System.out.println("   -> Policía: Denuncia de robo activa.");
                    }
                    if (!estado.dnrpaOk) {
                        System.out.println("   -> DNRPA: El solicitante no figura como titular registral.");
                    }
                    System.out.println("   Permanece inactivo (activo = false).");
                    System.out.println("=================================================================");
                }
            } else {
                System.err.println("[ERROR GATHER] Vehículo con ID " + vehiculoId + " no encontrado en MariaDB.");
            }

            // Limpieza del mapa en memoria para evitar memory leaks
            agregaciones.remove(vehiculoId);
        }
    }

    /**
     * Estructura interna para almacenar el avance de las validaciones en paralelo.
     */
    private static class EstadoValidacion {
        boolean policiaListo = false;
        boolean policiaOk = false;

        boolean dnrpaListo = false;
        boolean dnrpaOk = false;
    }
}