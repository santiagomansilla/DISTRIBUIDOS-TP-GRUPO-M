package com.unla.tp_distribuidos.consumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.unla.tp_distribuidos.config.RabbitMQConfig;
import com.unla.tp_distribuidos.dto.ReservaConfirmadaEvent;
import com.unla.tp_distribuidos.model.EstadoMantenimiento;
import com.unla.tp_distribuidos.dto.MantenimientoResultadoEvent;
@Component 
public class MantenimientoConsumer {
    private final RabbitTemplate rabbitTemplate;

	public MantenimientoConsumer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}


@RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVA_CONFIRMADA)

    public void ProcesarMantenimiento(ReservaConfirmadaEvent ReservaRecibida){

System.out.println("=================================================================");
        System.out.println("[MANTENIMIENTO SIMULADOR] Solicitud preventiva recibida para Reserva ID: " 
                + ReservaRecibida.getReservaId() + " (Vehículo ID: " + ReservaRecibida.getVehiculoId() + ")");
        System.out.println("[MANTENIMIENTO SIMULADOR] Período reservado: " 
                + ReservaRecibida.getFechaInicio() + " al " + ReservaRecibida.getFechaFin());

        try {
            // Simulamos una demora de inspección de taller (ej. 1.5 segundos)
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        

        MantenimientoResultadoEvent Mantenimiento = new MantenimientoResultadoEvent(ReservaRecibida.getReservaId(),ReservaRecibida.getVehiculoId(),EstadoMantenimiento.REALIZADO); 
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_MANTENIMIENTO_RESULTADO,Mantenimiento);
        
    
    
    }

    
}
