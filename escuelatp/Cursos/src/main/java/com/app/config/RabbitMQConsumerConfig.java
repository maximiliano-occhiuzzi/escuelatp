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
 * Configuracion de RabbitMQ del microservicio RECEPTOR (Cursos).
 *
 * Declara las mismas colas / exchange / bindings que el emisor: RabbitMQ no los
 * duplica si ya existen con los mismos parametros, y asi el consumer puede
 * arrancar aunque Alumno todavia no haya corrido.
 */
@Configuration
public class RabbitMQConsumerConfig {

    public static final String EXCHANGE = "alumno.exchange";

    public static final String QUEUE = "alumno.alta.queue";
    public static final String ROUTING_KEY = "alumno.alta.routingkey";

    public static final String BAJA_QUEUE = "alumno.baja.queue";
    public static final String BAJA_ROUTING_KEY = "alumno.baja.routingkey";

    @Bean
    public Queue alumnoAltaQueue() {
        return new Queue(QUEUE, true); // Cola duradera
    }

    @Bean
    public Queue alumnoBajaQueue() {
        return new Queue(BAJA_QUEUE, true);
    }

    @Bean
    public TopicExchange alumnoExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding alumnoAltaBinding(Queue alumnoAltaQueue, TopicExchange alumnoExchange) {
        return BindingBuilder.bind(alumnoAltaQueue).to(alumnoExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding alumnoBajaBinding(Queue alumnoBajaQueue, TopicExchange alumnoExchange) {
        return BindingBuilder.bind(alumnoBajaQueue).to(alumnoExchange).with(BAJA_ROUTING_KEY);
    }

    // Convierte el JSON del mensaje al objeto que recibe el @RabbitListener.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
