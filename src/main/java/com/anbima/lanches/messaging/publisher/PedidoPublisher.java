package com.anbima.lanches.messaging.publisher;

import com.anbima.lanches.dto.PedidoEvent;
import com.anbima.lanches.infra.amqp.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// MÓDULO A - Publisher/Produtor
// REQUISITO: Publicar { "pedidoId": id } na fila pedidos.recebidos ✅
@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(PedidoEvent event) {
        log.info("Publicando evento na fila {}: {}", RabbitMQConfig.QUEUE_NAME, event);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        log.info("Evento publicado com sucesso para pedidoId={}", event.getPedidoId());
    }
}
