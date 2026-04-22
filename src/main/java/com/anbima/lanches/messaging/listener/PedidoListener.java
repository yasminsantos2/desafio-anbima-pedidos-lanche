package com.anbima.lanches.messaging.listener;

import com.anbima.lanches.dto.PedidoEvent;
import com.anbima.lanches.infra.amqp.RabbitMQConfig;
import com.anbima.lanches.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// MÓDULO B - Listener/Consumer
// REQUISITO: Consumir mensagens da fila pedidos.recebidos ✅
@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoListener {

    private final PedidoService service;

    // @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumirMensagem(PedidoEvent event) {
        log.info("Mensagem recebida da fila {}: {}", RabbitMQConfig.QUEUE_NAME, event); // REQUISITO: Para cada
                                                                                        // mensagem: buscar o pedido no
                                                                                        // banco pelo ID
        try {
            // SIMULAÇÃO: Atraso de 8 segundos para simular processamento pesado/fila
            // Thread.sleep(5000);

            // REQUISITO: buscar o pedido no banco e atualizar o pedido correspondente para
            // status=ENTREGUE e persiste
            service.marcarComoEntregue(event.getPedidoId());
            log.info("Pedido {} atualizado para ENTREGUE com sucesso.", event.getPedidoId());
        } catch (Exception e) {
            // Estrutura preparada para ACK/NACK futuro
            log.error("Erro ao processar pedido {}: {}", event.getPedidoId(), e.getMessage());
        }
    }
}
