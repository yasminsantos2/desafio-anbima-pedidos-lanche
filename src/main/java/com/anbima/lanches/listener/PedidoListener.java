package com.anbima.lanches.listener;

import com.anbima.lanches.dto.PedidoEvent;
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

    // REQUISITO: Escutar a fila "pedidos.recebidos" ✅
    @RabbitListener(queues = "pedidos.recebidos")
    public void consumirMensagem(PedidoEvent event) {
        log.info("Mensagem recebida da fila pedidos.recebidos: {}", event);
        try {
            // REQUISITO: Buscar pedido pelo pedidoId e atualizar status = ENTREGUE ✅
            service.marcarComoEntregue(event.getPedidoId());
            log.info("Pedido {} atualizado para ENTREGUE com sucesso.", event.getPedidoId());
        } catch (Exception e) {
            // Estrutura preparada para ACK/NACK futuro
            log.error("Erro ao processar pedido {}: {}", event.getPedidoId(), e.getMessage());
        }
    }
}
