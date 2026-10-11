package com.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ del microservicio EMISOR (Alumno).
 *
 * Recorrido de un mensaje:
 *   AlumnoServiceImpl -> EXCHANGE --(ROUTING_KEY)--> QUEUE -> (consumer en Cursos)
 *
 * Hay dos eventos, ambos por el mismo exchange (topic) pero con distinta
 * routing key y distinta cola:
 *   - alta: un alumno se inscribio en un curso   -> alumno.alta.queue
 *   - baja: un alumno se deshabilito (baja logica) -> alumno.baja.queue
 *
 * Los nombres tienen que ser IGUALES en el emisor y en el consumer.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "alumno.exchange";

    public static final String QUEUE = "alumno.alta.queue";
    public static final String ROUTING_KEY = "alumno.alta.routingkey";

    public static final String BAJA_QUEUE = "alumno.baja.queue";
    public static final String BAJA_ROUTING_KEY = "alumno.baja.routingkey";

    // Colas duraderas: sobreviven a un reinicio del broker
    @Bean
    public Queue alumnoAltaQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Queue alumnoBajaQueue() {
        return new Queue(BAJA_QUEUE, true);
    }

    @Bean
    public TopicExchange alumnoExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Une cada cola al exchange: lo que llega con esa routing key va a esa cola
    @Bean
    public Binding alumnoAltaBinding(Queue alumnoAltaQueue, TopicExchange alumnoExchange) {
        return BindingBuilder.bind(alumnoAltaQueue).to(alumnoExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding alumnoBajaBinding(Queue alumnoBajaQueue, TopicExchange alumnoExchange) {
        return BindingBuilder.bind(alumnoBajaQueue).to(alumnoExchange).with(BAJA_ROUTING_KEY);
    }

    // Convierte automaticamente los objetos Java a JSON al enviarlos.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
