import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidoService } from '../../services/pedido.service';
import { Pedido } from '../../models/pedido.model';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css'
})
export class PedidosComponent implements OnInit {
  private pedidoService = inject(PedidoService);
  
  protected pedidos = signal<Pedido[]>([]);
  protected loading = signal(false);
  protected error = signal<string | null>(null);

  ngOnInit() {
    this.carregarPedidos();
  }

  carregarPedidos() {
    this.loading.set(true);
    this.error.set(null);
    
    this.pedidoService.listarPedidos().subscribe({
      next: (res) => {
        const ordenados = res.sort((a, b) => b.id - a.id);
        this.pedidos.set(ordenados);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao listar pedidos:', err);
        this.error.set('Não foi possível carregar os pedidos. Verifique o backend.');
        this.loading.set(false);
      }
    });
  }

  finalizarPedido(id: number) {
    this.loading.set(true);
    this.pedidoService.marcarComoEntregue(id).subscribe({
      next: () => {
        this.carregarPedidos();
      },
      error: (err) => {
        console.error('Erro ao finalizar pedido:', err);
        this.error.set('Erro ao marcar pedido como entregue.');
        this.loading.set(false);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'RECEBIDO': return 'status-received';
      case 'ENTREGUE': return 'status-delivered';
      default: return '';
    }
  }
}
