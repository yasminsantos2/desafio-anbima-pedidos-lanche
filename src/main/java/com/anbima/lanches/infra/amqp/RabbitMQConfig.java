package com.anbima.lanches.infra.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "pedidos.exchange.v2";
    public static final String QUEUE_NAME = "pedidos.simulado.v2";
    public static final String ROUTING_KEY = "pedidos.routing.v2";

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue pedidosQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding pedidosBinding(Queue pedidosQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(pedidosQueue).to(pedidosExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
