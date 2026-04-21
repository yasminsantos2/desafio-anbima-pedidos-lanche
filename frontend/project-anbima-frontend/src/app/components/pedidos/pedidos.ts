import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidoService } from '../../services/pedido.service';
import { Pedido } from '../../models/pedido.model';
import { switchMap } from 'rxjs';

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
    this.carregarPedidos(false);
  }

  carregarPedidos(processar: boolean = false) {
    this.loading.set(true);
    this.error.set(null);
    
    const obs$ = processar 
      ? this.pedidoService.processarFila().pipe(switchMap(() => this.pedidoService.listarPedidos()))
      : this.pedidoService.listarPedidos();

    obs$.subscribe({
      next: (res) => {
        // Ordenação decrescente por ID para mostrar os mais recentes no topo
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

  getStatusClass(status: string): string {
    switch (status) {
      case 'RECEBIDO': return 'status-received';
      case 'ENTREGUE': return 'status-delivered';
      default: return '';
    }
  }
}
