package com.unla.tp_distribuidos.consumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.MantenimientoResultadoEvent;

import com.unla.tp_distribuidos.service.ReservaService;

@Component 
public class ReservaConsumer {

private final ReservaService reservaService;
	public ReservaConsumer(ReservaService reservaService ) {
		
        this.reservaService = reservaService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_MANTENIMIENTO_RESULTADO)

    public void ProcesarResultadoMantenimiento(MantenimientoResultadoEvent resultado){
try{
        reservaService.UpdateReservaMantenimiento(resultado.getReservaId(),resultado.getResultado());
}catch(Exception e){

    System.err.println("[RENTAR CONSUMER] Error al registrar mantenimiento: " + e.getMessage());
}
    } 


}
