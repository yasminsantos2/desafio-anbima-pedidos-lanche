package com.anbima.lanches.controller;

import com.anbima.lanches.domain.Pedido;
import com.anbima.lanches.domain.StatusPedido;
import com.anbima.lanches.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Gestão de Pedidos", description = "Endpoints para criação, consulta e finalização de pedidos")
public class PedidoController {

    private final PedidoService service;

    @Operation(summary = "Criar pedido via string posicional", 
               description = "Recebe uma string de 40 caracteres contendo: Tipo(10), Proteína(10), Acompanhamento(10), Quantidade(2) e Bebida(8).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso", 
                     content = @Content(schema = @Schema(implementation = Pedido.class))),
        @ApiResponse(responseCode = "400", description = "Payload inválido ou fora do padrão de 40 caracteres")
    })
    @PostMapping(value = "/posicional", consumes = "text/plain")
    public ResponseEntity<?> receberPedidoPosicional(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "String posicional de 40 caracteres",
                required = true,
                content = @Content(examples = {
                    @ExampleObject(name = "Combo Hamburguer (10% Desc)", 
                                   value = "HAMBURGUERCARNE     SALADA    01COCA    ",
                                   description = "Hamburguer + Carne + Salada aplica desconto de 10%"),
                    @ExampleObject(name = "Pedido Pastel", 
                                   value = "PASTEL    FRANGO    BACON     02SUCO    ",
                                   description = "Pedido comum de 2 pastéis"),
                    @ExampleObject(name = "Pedido Hotdog (Outros)", 
                                   value = "HOTDOG    SALSICHA  BATATA    01COCA    ",
                                   description = "Exemplo de lanche que não é Hamburguer ou Pastel")
                })
            )
            @RequestBody(required = false) String payload) {
        try {
            Pedido pedido = service.processarPedidoPosicional(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Listar todos os pedidos", description = "Retorna uma lista de todos os pedidos cadastrados no sistema.")
    @GetMapping
    public java.util.List<Pedido> listar() {
        return service.listarTodos();
    }

    @Operation(summary = "Buscar pedido por ID", description = "Retorna os detalhes de um pedido específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Marcar pedido como entregue", 
               description = "Valida se existe uma mensagem para este pedido na fila do RabbitMQ. Se houver, consome a mensagem e atualiza o status para ENTREGUE.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido processado e entregue"),
        @ApiResponse(responseCode = "400", description = "Pedido não encontrado na fila do RabbitMQ ou erro no processamento")
    })
    @PostMapping("/{id}/entregar")
    public ResponseEntity<Void> entregar(@PathVariable Long id) {
        service.processarPedidoEspecificoDaFila(id);
        return ResponseEntity.ok().build();
    }
}