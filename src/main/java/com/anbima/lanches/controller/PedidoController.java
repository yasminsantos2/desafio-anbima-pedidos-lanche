package com.anbima.lanches.controller;

import com.anbima.lanches.domain.Pedido;
import com.anbima.lanches.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}