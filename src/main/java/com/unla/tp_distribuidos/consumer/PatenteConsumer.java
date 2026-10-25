package com.unla.tp_distribuidos.consumer;

import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.VehiculoVerificacionResultadoEvent;
import com.unla.tp_distribuidos.model.Vehiculo;
import com.unla.tp_distribuidos.repository.VehiculoRepository;
import com.unla.tp_distribuidos.service.ValidacionDnrpaPolicialService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * CONSUMIDOR DE DOMINIO RENTAR.
 * Procesa las respuestas policiales del broker y bifurca según el origen de la operación.
 */
@Component
public class PatenteConsumer {

    private final VehiculoRepository vehiculoRepository;
    private final ValidacionDnrpaPolicialService ValidacionDnrpaPolicialService;

    public PatenteConsumer(VehiculoRepository vehiculoRepository, 
                           ValidacionDnrpaPolicialService ValidacionDnrpaPolicialService) {
        this.vehiculoRepository = vehiculoRepository;
        this.ValidacionDnrpaPolicialService = ValidacionDnrpaPolicialService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VERIF_RESULTADO)
    public void procesarResultadoPolicial(VehiculoVerificacionResultadoEvent event) {
        System.out.println("[RENTAR CONSUMER] Procesando informe policial para vehículo ID: " 
                + event.getVehiculoId() + " (Origen: " + event.getOrigen() + ")");

        boolean noRobado = !"ROBADO".equalsIgnoreCase(event.getResultado());

        // ---------------------------------------------------------------------
        // CASO 1: VEHÍCULO EXTERNO (Requiere coordinación con DNRPA)
        // ---------------------------------------------------------------------
        if ("INCORPORACION_EXTERNA".equalsIgnoreCase(event.getOrigen())) {
            System.out.println("[RENTAR GATHER] Resultado policial recibido para incorporación externa (Patente: " 
                    + event.getPatente() + "). Derivando al agregador...");
            
            // Notifica al servicio agregador si pasó o no la prueba policial
            ValidacionDnrpaPolicialService.registrarResultadoPolicia(event.getVehiculoId(), noRobado);
            return;
        }

        // ---------------------------------------------------------------------
        // CASO 2: FLOTA PROPIA / ALTA ADMINISTRADOR (Comportamiento original)
        // ---------------------------------------------------------------------
        if (!noRobado) {
            Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(event.getVehiculoId());
            if (vehiculoOpt.isPresent()) {
                Vehiculo v = vehiculoOpt.get();
                // Regla del TP: no inventar estados, se inhabilita lógicamente[cite: 6]
                v.setActivo(false);
                vehiculoRepository.save(v);
                System.out.println("[RENTAR ALERTA] El vehículo con patente " + v.getPatente() 
                        + " tiene denuncia de robo. Quedó inhabilitado (activo=false).");
            } else {
                System.err.println("[RENTAR ERROR] Vehículo con ID " + event.getVehiculoId() 
                        + " no encontrado en base de datos.");
            }
        } else {
            System.out.println("[RENTAR OK] Verificación policial exitosa para patente " 
                    + event.getPatente() + ". Operatividad normal.");
        }
    }
}