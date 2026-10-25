package com.unla.tp_distribuidos.consumer;

import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.VehiculoVerificacionResultadoEvent;
import com.unla.tp_distribuidos.dto.VehiculoVerificacionSolicitadaEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * SIMULADOR EXTERNO: POLICÍA
 * Representa un sistema ajeno a Rentar. No interactúa con MariaDB/JPA.
 */
@Component
public class PolicialConsumer {

    private final RabbitTemplate rabbitTemplate;

    // Simulador: base de patentes con pedido de secuestro / robadas
    private static final Set<String> PATENTES_ROBADAS = Set.of("ROB123", "DEN999", "AAA000","ROB444");

    
    public PolicialConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VERIF_SOLICITADA)
    public void simularVerificacionPolicial(VehiculoVerificacionSolicitadaEvent event) {
        System.out.println("[POLICIA SIMULADOR] Recibida verificacion para patente: " + event.getPatente());

        // Regla simulada: si coincide con la lista negra o contiene "ROB"
        boolean esRobado = PATENTES_ROBADAS.contains(event.getPatente()) || event.getPatente().contains("ROB");
        String resultado = esRobado ? "ROBADO" : "VALIDADO";

        VehiculoVerificacionResultadoEvent respuesta = new VehiculoVerificacionResultadoEvent(
                event.getVehiculoId(),
                event.getPatente(),
                resultado,
                event.getOrigen() // Mantiene el contexto ("FLOTA_PROPIA" o "INCORPORACION_EXTERNA")
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_VERIF_RESULTADO, respuesta);
        System.out.println("[POLICIA SIMULADOR] Resultado emitido: " + resultado + " para patente: " + event.getPatente());
    }
}