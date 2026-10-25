package com.unla.tp_distribuidos.consumer;

import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.TitularidadResultadoDTO;
import com.unla.tp_distribuidos.dto.TitularidadSolicitudDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DnrpaSimuladorConsumer {

    private final RabbitTemplate rabbitTemplate;

    public DnrpaSimuladorConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TITULARIDAD_SOLICITADA)
    public void procesarTitularidad(TitularidadSolicitudDTO solicitud) {
        System.out.println("[DNRPA SIMULADOR] Recibida consulta de titularidad para patente: " 
                + solicitud.getPatente() + " con DNI: " + solicitud.getDocumentoCliente());

        // Regla de simulación: si el documento no es nulo y no termina en 9, es titular legítimo
        boolean esTitular = solicitud.getDocumentoCliente() != null && (solicitud.getDocumentoCliente() % 10 != 9);

        TitularidadResultadoDTO resultado = new TitularidadResultadoDTO(
                solicitud.getVehiculoId(),
                solicitud.getPatente(),
                esTitular,
                esTitular ? "Titularidad validada correctamente en DNRPA" : "El solicitante no figura como titular del dominio"
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_TITULARIDAD_RESULTADO, resultado);
        System.out.println("[DNRPA SIMULADOR] Dictamen emitido -> " + (esTitular ? "ES TITULAR" : "NO ES TITULAR"));
    }
}