package com.anbima.lanches.messaging.publisher;

import com.anbima.lanches.dto.PedidoEvent;
import com.anbima.lanches.infra.amqp.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PedidoPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PedidoPublisher publisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Teste: publicar envia mensagem com exchange, routing key e evento corretos
    @Test
    void devePublicarEventoNoRabbitMQ() {
        PedidoEvent event = new PedidoEvent(1L);

        publisher.publicar(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                eq(event)
        );
    }

    // Teste: publicar com pedidoId diferente
    @Test
    void devePublicarEventoComPedidoIdCorreto() {
        PedidoEvent event = new PedidoEvent(99L);

        publisher.publicar(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                eq(event)
        );
        verifyNoMoreInteractions(rabbitTemplate);
    }

    // Teste: erro de conexão propaga a exceção
    @Test
    void devePropararExcecaoQuandoRabbitMQFalhar() {
        PedidoEvent event = new PedidoEvent(1L);

        doThrow(new AmqpException("Conexão recusada"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(PedidoEvent.class));

        assertThrows(AmqpException.class, () -> publisher.publicar(event));
    }
}
