package com.anbima.lanches.controller;

import com.anbima.lanches.domain.Pedido;
import com.anbima.lanches.domain.StatusPedido;
import com.anbima.lanches.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    // REQUISITO: POST /pedidos/posicional
    // REQUISITO: Recebe uma linha posicional de 40 caracteres (Content-Type: text/plain)
    @PostMapping(value = "/posicional", consumes = "text/plain")
    public ResponseEntity<?> receberPedidoPosicional(@RequestBody(required = false) String payload) {
        try {
            // REQUISITO: Transformar a string em um objeto Pedido
            Pedido pedido = service.processarPedidoPosicional(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // MÓDULO B - API de consulta
    // REQUISITO: GET /pedidos → Listar todos os pedidos ✅
    @GetMapping
    public java.util.List<Pedido> listar() {
        return service.listarTodos();
    }

    // REQUISITO: GET /pedidos/{id} → Consultar um pedido específico ✅
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // NOVO: PATCH /pedidos/{id}/status → Atualizar status manualmente ✅
    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        StatusPedido status = StatusPedido.valueOf(statusStr);
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }

    @PostMapping("/processar-fila")
    public ResponseEntity<Void> processarFila() {
        service.processarFila();
        return ResponseEntity.ok().build();
    }
}