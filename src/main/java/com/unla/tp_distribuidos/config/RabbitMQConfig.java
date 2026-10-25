package com.unla.tp_distribuidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombres exactos pedidos por el TP
    public static final String QUEUE_VERIF_SOLICITADA = "vehiculos.verificacion.solicitada";
    public static final String QUEUE_VERIF_RESULTADO = "vehiculos.verificacion.resultado";

    public static final String QUEUE_TITULARIDAD_SOLICITADA = "vehiculos.titularidad.solicitada";
    public static final String QUEUE_TITULARIDAD_RESULTADO = "vehiculos.titularidad.resultado";





    public static final String QUEUE_RESERVA_CONFIRMADA = "reserva.confirmada";
    public static final String QUEUE_MANTENIMIENTO_RESULTADO = "mantenimiento.resultado";

    // 1. Declaración de Colas (Durable para no perder datos si cae el broker)
   // 1. Verificación Policial (Vehículo Propio)
    @Bean
    public Queue queueVerifSolicitada() { return new Queue(QUEUE_VERIF_SOLICITADA, true);}

    @Bean
    public Queue queueVerifResultado() { return new Queue(QUEUE_VERIF_RESULTADO, true);}

    // 2. Validación de Titularidad DNRPA (Vehículo Externo)
    @Bean
    public Queue queueTitularidadSolicitada() { return new Queue(QUEUE_TITULARIDAD_SOLICITADA, true);}
    
    @Bean
    public Queue queueTitularidadResultado() { return new Queue(QUEUE_TITULARIDAD_RESULTADO, true);}


    //3.Mantenimiento
    
    @Bean public Queue queueReservaConfirmada() { return new Queue(QUEUE_RESERVA_CONFIRMADA, true); }
    @Bean public Queue queueMantenimientoResultado() { return new Queue(QUEUE_MANTENIMIENTO_RESULTADO, true); }

    // 2. Serializador JSON automático para los eventos
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}