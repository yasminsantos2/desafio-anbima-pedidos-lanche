package com.anbima.lanches.service;

import com.anbima.lanches.domain.Pedido;
import com.anbima.lanches.domain.StatusPedido;
import com.anbima.lanches.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import com.anbima.lanches.infra.amqp.RabbitMQConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.anbima.lanches.dto.PedidoEvent;

class PedidoServiceTest {

    @Mock
    private PedidoRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PedidoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveProcessarPedidoComDescontoExemplo1() {
        // REQUISITO: HAMBURGUER + CARNE + SALADA, 1 unid (10% desconto)
        // Layout: HAMBURGUER(10) + CARNE(10) + SALADA(10) + 01(2) + COCA(8)
        String payload = "HAMBURGUERCARNE     SALADA    01COCA    ";
        
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        Pedido pedido = service.processarPedidoPosicional(payload);

        // VALOR ESPERADO: 20,00 com 10% -> 18,00
        assertEquals(new BigDecimal("18.0000"), pedido.getValor());
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY), any(PedidoEvent.class));
    }

    @Test
    void deveProcessarPedidoSemDescontoExemplo2() {
        // REQUISITO: PASTEL + FRANGO + BACON, 2 unid (Sem desconto)
        // Layout: PASTEL(10) + FRANGO(10) + BACON(10) + 02(2) + SUCO(8)
        String payload = "PASTEL    FRANGO    BACON     02SUCO    ";
        
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        Pedido pedido = service.processarPedidoPosicional(payload);

        // VALOR ESPERADO: 15,00 * 2 -> 30,00
        assertEquals(new BigDecimal("30.00"), pedido.getValor());
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY), any(PedidoEvent.class));
    }

    @Test
    void deveCalcularPrecoParaOutrosLanches() {
        String payload = "HOTDOG    SALSICHA  BATATA    01COCA    ";
        
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        Pedido pedido = service.processarPedidoPosicional(payload);

        // VALOR ESPERADO: Outros(12) * 1 -> 12,00
        assertEquals(new BigDecimal("12.00"), pedido.getValor());
    }

    @Test
    void deveLancarExcecaoQuandoPayloadInvalido() {
        String payloadCurto = "HAMBURGUER";
        assertThrows(IllegalArgumentException.class, () -> service.processarPedidoPosicional(payloadCurto));
    }

    // Teste de sucesso para marcar como entregue
    @Test
    void deveMarcarPedidoComoEntregueComSucesso() {
        Long id = 1L;
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setStatus(StatusPedido.RECEBIDO);

        when(repository.findById(id)).thenReturn(java.util.Optional.of(pedido));
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        service.marcarComoEntregue(id);

        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
        org.mockito.Mockito.verify(repository).findById(id);
        org.mockito.Mockito.verify(repository).save(pedido);
    }

    // Teste de erro quando o pedido não é encontrado
    @Test
    void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(java.util.Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.marcarComoEntregue(id));
        assertEquals("Pedido não encontrado: " + id, exception.getMessage());
    }
}
