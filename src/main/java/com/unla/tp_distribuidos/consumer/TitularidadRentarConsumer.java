package com.unla.tp_distribuidos.consumer;

import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.TitularidadResultadoDTO;
import com.unla.tp_distribuidos.service.ValidacionDnrpaPolicialService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * CONSUMIDOR DE DOMINIO RENTAR (RAMA DNRPA).
 * Escucha la cola de resultados registrales y reporta al agregador Scatter-Gather.
 */
@Component
public class TitularidadRentarConsumer {

    private final ValidacionDnrpaPolicialService validacionDnrpaPolicialService;

    public TitularidadRentarConsumer(ValidacionDnrpaPolicialService validacionDnrpaPolicialService) {
        this.validacionDnrpaPolicialService = validacionDnrpaPolicialService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TITULARIDAD_RESULTADO)
    public void recibirResultadoTitularidad(TitularidadResultadoDTO resultado) {
        System.out.println("[RENTAR CONSUMER - DNRPA] Recibido informe de titularidad para vehículo ID: " 
                + resultado.getVehiculoId() + " (Patente: " + resultado.getPatente() + ")");

        // Delega la respuesta al servicio agregador para cruzarla con el informe policial
        validacionDnrpaPolicialService.registrarResultadoTitularidad(
                resultado.getVehiculoId(), 
                resultado.isEsTitular()
        );
    }
}