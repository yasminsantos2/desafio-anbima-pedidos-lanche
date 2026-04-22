package com.anbima.lanches.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @io.swagger.v3.oas.annotations.media.Schema(description = "ID único do pedido", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Tipo do lanche (ex: HAMBURGUER, PASTEL)", example = "HAMBURGUER")
    @Column(name = "tipo_lanche", length = 20)
    private String tipoLanche;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Proteína do lanche", example = "CARNE")
    @Column(length = 20)
    private String proteina;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Acompanhamento do lanche", example = "SALADA")
    @Column(length = 20)
    private String acompanhamento;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Quantidade de itens", example = "01")
    private Integer quantidade;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Bebida do pedido", example = "COCA")
    @Column(length = 20)
    private String bebida;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Valor total calculado do pedido", example = "18.00")
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Status atual do pedido", example = "RECEBIDO")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusPedido status;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Data e hora de criação do pedido", accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY)
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusPedido.RECEBIDO;
        }
    }
}
