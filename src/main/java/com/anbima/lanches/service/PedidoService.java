package com.anbima.lanches.service;

import com.anbima.lanches.domain.Pedido;
import com.anbima.lanches.domain.StatusPedido;
import com.anbima.lanches.dto.PedidoEvent;
import com.anbima.lanches.infra.amqp.RabbitMQConfig;
import com.anbima.lanches.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Pedido salvar(Pedido pedido) {
        return repository.save(pedido);
    }

    // MÓDULO B - REQUISITO: Listar todos os pedidos ✅
    public List<Pedido> listarTodos() {
        return repository.findAll();
    }

    // RF-05: Mapear a linha posicional para um objeto Pedido
    @Transactional
    public Pedido processarPedidoPosicional(String payload) {
        // REQUISITO: Validações da string posicional
        validarPayload(payload);

        // REQUISITO: Transformar a string em um objeto Pedido (Conversão)
        String tipoLanche = payload.substring(0, 10).trim();
        String proteina = payload.substring(10, 20).trim();
        String acompanhamento = payload.substring(20, 30).trim();
        Integer quantidade = Integer.parseInt(payload.substring(30, 32));
        String bebida = payload.substring(32, 40).trim();

        BigDecimal valorTotal = calcularValor(tipoLanche, proteina, acompanhamento, bebida, quantidade);

        Pedido pedido = new Pedido();
        pedido.setTipoLanche(tipoLanche);
        pedido.setProteina(proteina);
        pedido.setAcompanhamento(acompanhamento);
        pedido.setQuantidade(quantidade);
        pedido.setBebida(bebida);
        pedido.setValor(valorTotal);
        pedido.setStatus(StatusPedido.RECEBIDO); // RF-08: Persistir o pedido no banco com status inicial RECEBIDO

        Pedido pedidoSalvo = repository.save(pedido);

        // MÓDULO A - Disparar evento local (será publicado no RabbitMQ após o commit)
        eventPublisher.publishEvent(new PedidoEvent(pedidoSalvo.getId()));

        return pedidoSalvo;
    }

    // RF-06: Calcular preço base (HAMBURGUER=20, PASTEL=15, outros=12)
    private BigDecimal calcularValor(String tipo, String proteina, String acompanhamento, String bebida, int quantidade) {
        BigDecimal precoBase;
        
        // REQUISITO: Preço base por tipo de lanche
        if ("HAMBURGUER".equalsIgnoreCase(tipo)) {
            precoBase = new BigDecimal("20.00");
        } else if ("PASTEL".equalsIgnoreCase(tipo)) {
            precoBase = new BigDecimal("15.00");
        } else {
            precoBase = new BigDecimal("12.00");
        }
        
        BigDecimal subtotal = precoBase.multiply(new BigDecimal(quantidade));
        
        // RF-07: Aplicar 10% de desconto quando o pedido for HAMBURGUER + CARNE + SALADA
        if ("HAMBURGUER".equalsIgnoreCase(tipo) && 
            "CARNE".equalsIgnoreCase(proteina) && 
            "SALADA".equalsIgnoreCase(acompanhamento)) {
            BigDecimal desconto = subtotal.multiply(new BigDecimal("0.10"));
            return subtotal.subtract(desconto);
        }
        
        return subtotal;
    }

    // MÓDULO B - REQUISITO: Consultar pedido específico por ID ✅
    public Pedido buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
    }

    @Transactional
    public void processarPedidoEspecificoDaFila(Long targetId) {
        // REQUISITO: Valida se o pedido está na fila do RabbitMQ
        log.info("Buscando pedido {} na fila para processamento granular...", targetId);
        
        int maxAttempts = 100; 
        int attempts = 0;
        boolean encontrado = false;

        while (attempts < maxAttempts) {
            // REQUISITO: Consome a mensagem para validar sua existência física na fila
            Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.QUEUE_NAME);
            if (message == null) break;

            log.info("Objeto cru recebido da fila: class={}, content={}", 
                     message.getClass().getName(), 
                     message);

            Long extractedId = extrairIdDaMensagem(message);
            log.info("Mensagem lida da fila. ID extraído: {}", extractedId);
            
            if (extractedId != null && extractedId.equals(targetId)) {
                log.info("Pedido {} encontrado na fila! Processando...", targetId);
                marcarComoEntregue(targetId);
                encontrado = true;
                break;
            } else {
                // Devolve para a fila para outros processamentos
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
                log.debug("Mensagem id={} não é o alvo. Reenfileirada.", extractedId);
            }
            attempts++;
        }

        if (!encontrado) {
            log.warn("Fim da busca na fila. Pedido {} não foi encontrado após {} tentativas.", targetId, attempts);
            throw new IllegalArgumentException("O pedido " + targetId + " não está disponível para finalização na fila. Ele pode já ter sido processado ou ainda não foi enfileirado.");
        }
    }

    private Long extrairIdDaMensagem(Object message) {
        if (message instanceof PedidoEvent) {
            return ((PedidoEvent) message).getPedidoId();
        } else if (message instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) message;
            Object idObj = map.get("pedidoId");
            return idObj != null ? Long.valueOf(idObj.toString()) : null;
        }
        return null;
    }

    // MÓDULO B - REQUISITO: buscar o pedido no banco e atualizar o pedido correspondente para status=ENTREGUE e persiste
    @Transactional
    public void marcarComoEntregue(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        pedido.setStatus(StatusPedido.ENTREGUE);
        repository.save(pedido);
    }

    public void validarPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("O payload não pode ser nulo ou vazio.");
        }

        // RF-02: A linha deve ter exatamente 40 caracteres
        if (payload.length() != 40) {
            throw new IllegalArgumentException(
                    "O payload deve ter exatamente 40 caracteres. Tamanho recebido: " + payload.length()
            );
        }

        String tipoLanche = payload.substring(0, 10);
        String proteina = payload.substring(10, 20);
        String acompanhamento = payload.substring(20, 30);
        String quantidadeStr = payload.substring(30, 32);
        String bebida = payload.substring(32, 40);

        // REQUISITO: Campos alfanuméricos (A)
        validarCampoAlfanumerico(tipoLanche, "tipoLanche");
        validarCampoAlfanumerico(proteina, "proteina");
        validarCampoAlfanumerico(acompanhamento, "acompanhamento");
        validarCampoAlfanumerico(bebida, "bebida");

        // REQUISITO: Campos numéricos (N)
        validarCampoNumerico(quantidadeStr, "quantidade");

        // RF-04: Quantidade deve ser numérica e estar entre 01 e 99
        int quantidade = Integer.parseInt(quantidadeStr);
        if (quantidade < 1 || quantidade > 99) {
            throw new IllegalArgumentException("O campo quantidade deve estar entre 01 e 99.");
        }
    }

    private void validarCampoAlfanumerico(String valor, String nomeCampo) {
        // RF-03: Campos do tipo A devem aceitar letras, números e espaços (Alfanuméricos)
        if (!valor.matches("[A-Za-z0-9 ]+")) {
            throw new IllegalArgumentException(
                    "O campo " + nomeCampo + " deve conter apenas letras, números e espaços."
            );
        }
    }

    private void validarCampoNumerico(String valor, String nomeCampo) {
        if (!valor.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "O campo " + nomeCampo + " deve conter apenas números."
            );
        }
    }
}