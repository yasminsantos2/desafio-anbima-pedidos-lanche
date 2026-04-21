package com.anbima.lanches.messaging.publisher;

import com.anbima.lanches.dto.PedidoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoEventListener {

    private final PedidoPublisher pedidoPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePedidoCriadoEvent(PedidoEvent event) {
        log.info("Transação confirmada. Iniciando publicação do pedido {} no RabbitMQ.", event.getPedidoId());
        pedidoPublisher.publicar(event);
    }
}
