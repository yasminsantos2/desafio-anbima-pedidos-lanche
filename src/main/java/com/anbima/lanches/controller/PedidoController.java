package com.anbima.lanches.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @PostMapping("/posicional")
    public String receberPedidoPosicional(@RequestBody String payload) {
        // Log para fins de debug no console
        System.out.println("Processing positional payload (mock): " + payload);
        
        // Retorno mocado confirmando o recebimento da string
        return "Recebido com sucesso! String posicional processada: " + payload;
    }
}
