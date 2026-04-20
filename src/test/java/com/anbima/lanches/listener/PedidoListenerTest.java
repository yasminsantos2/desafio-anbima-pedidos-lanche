package com.anbima.lanches.listener;

import com.anbima.lanches.dto.PedidoEvent;
import com.anbima.lanches.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class PedidoListenerTest {

    @Mock
    private PedidoService service;

    @InjectMocks
    private PedidoListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Teste para garantir que o listener chama o service corretamente
    @Test
    void deveChamarServiceAoReceberEvento() {
        Long pedidoId = 1L;
        PedidoEvent event = new PedidoEvent(pedidoId);

        listener.consumirMensagem(event);

        verify(service).marcarComoEntregue(pedidoId);
    }
}
