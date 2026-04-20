package com.anbima.lanches.messaging.listener;

import com.anbima.lanches.dto.PedidoEvent;
import com.anbima.lanches.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class PedidoListenerTest {

    @Mock
    private PedidoService service;

    @InjectMocks
    private PedidoListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Teste: listener chama marcarComoEntregue com o pedidoId correto
    @Test
    void deveChamarServiceAoReceberEvento() {
        Long pedidoId = 1L;
        PedidoEvent event = new PedidoEvent(pedidoId);

        listener.consumirMensagem(event);

        verify(service).marcarComoEntregue(pedidoId);
    }

    // Teste: listener trata exceção sem propagar (evita requeue infinito)
    @Test
    void deveCapturarExcecaoQuandoServiceFalhar() {
        Long pedidoId = 99L;
        PedidoEvent event = new PedidoEvent(pedidoId);

        doThrow(new RuntimeException("Pedido não encontrado: " + pedidoId))
                .when(service).marcarComoEntregue(pedidoId);

        // Não deve lançar exceção — o listener captura internamente
        listener.consumirMensagem(event);

        verify(service).marcarComoEntregue(pedidoId);
    }

    // Teste: listener processa pedidoId diferente
    @Test
    void deveProcessarPedidoIdsDiferentes() {
        PedidoEvent event = new PedidoEvent(42L);

        listener.consumirMensagem(event);

        verify(service).marcarComoEntregue(42L);
        verifyNoMoreInteractions(service);
    }
}
