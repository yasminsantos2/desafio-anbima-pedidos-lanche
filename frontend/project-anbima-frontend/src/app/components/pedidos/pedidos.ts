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
        this.pedidos.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao listar pedidos:', err);
        let errorMessage = 'Não foi possível conectar ao servidor. Verifique se o backend está rodando na porta 8081.';
        
        if (typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }

        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  ciclarStatus(pedido: Pedido) {
    const statusOrdem: string[] = ['RECEBIDO', 'EM_PREPARACAO', 'ENTREGUE'];
    const indexAtual = statusOrdem.indexOf(pedido.status);
    const proximoIndex = (indexAtual + 1) % statusOrdem.length;
    
    // Atualiza localmente (Mock)
    pedido.status = statusOrdem[proximoIndex] as any;
    
    // Opcional: Feedback visual de que foi "alterado"
    console.log(`Status do pedido #${pedido.id} alterado para ${pedido.status} (Mock)`);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'RECEBIDO': return 'status-received';
      case 'EM_PREPARACAO': return 'status-preparing';
      case 'ENTREGUE': return 'status-delivered';
      default: return '';
    }
  }
}
