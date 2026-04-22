import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PedidoService } from '../../services/pedido.service';
import { Pedido } from '../../models/pedido.model';

@Component({
  selector: 'app-novo-pedido',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './novo-pedido.html',
  styleUrl: './novo-pedido.css'
})
export class NovoPedidoComponent {
  private pedidoService = inject(PedidoService);

  // Payload principal (40 caracteres)
  protected payload = signal('');
  
  // RF-14: Campos Auxiliares para Mapeamento (Assistente)
  protected formLanche = signal('');
  protected formProteina = signal('');
  protected formAcompanhamento = signal('');
  protected formQuantidade = signal('');
  protected formBebida = signal('');

  protected result = signal<Pedido | null>(null);
  protected error = signal<string | null>(null);
  protected loading = signal(false);

  // REQUISITOS DE PREENCHIMENTO:
  // Tipo A = Alfanumérico (letras, números e espaços) -> Espaços à Direita
  // Tipo N = Numérico (apenas dígitos) -> Zeros à Esquerda
  formatarEAtualizar() {
    const lanche = this.formLanche().toUpperCase().substring(0, 10).padEnd(10, ' ');
    const proteina = this.formProteina().toUpperCase().substring(0, 10).padEnd(10, ' ');
    const acompanhamento = this.formAcompanhamento().toUpperCase().substring(0, 10).padEnd(10, ' ');
    
    let qtdRaw = this.formQuantidade().replace(/\D/g, '');
    let qtd = '01'; // Default mínimo
    if (qtdRaw) {
      qtd = qtdRaw.substring(0, 2).padStart(2, '0');
    }
    
    const bebida = this.formBebida().toUpperCase().substring(0, 8).padEnd(8, ' ');

    // Layout Final: 10 + 10 + 10 + 2 + 8 = 40
    const novaString = `${lanche}${proteina}${acompanhamento}${qtd}${bebida}`;
    this.payload.set(novaString);
  }

  validar(text: string): string[] {
    const errors: string[] = [];

    if (text.length !== 40) {
      errors.push(`A linha deve ter exatamente 40 caracteres (atual: ${text.length}).`);
      return errors;
    }

    // Regras básicas de tipo (Mais validações são feitas no Backend)
    const quantidadeStr = text.substring(30, 32);
    if (!/^\d+$/.test(quantidadeStr)) {
      errors.push('O campo Quantidade deve conter apenas dígitos (Posições 31-32).');
    }

    return errors;
  }

  enviar() {
    const text = this.payload();
    const validationErrors = this.validar(text);

    if (validationErrors.length > 0) {
      this.error.set(validationErrors.join(' | '));
      this.result.set(null);
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.result.set(null);

    this.pedidoService.enviarPedidoPosicional(text).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
        this.limparFormulario();
      },
      error: (err) => {
        console.error('Erro retornado pelo Servidor:', err);
        let errorMessage = 'O servidor rejeitou o pedido.';

        // REQUISITO: Mostrar o erro do backend no front para ajudar o usuário
        if (err.status === 0) {
          errorMessage = 'Não foi possível conectar ao servidor (8080).';
        } else if (typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }

        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  private limparFormulario() {
    this.payload.set('');
    this.formLanche.set('');
    this.formProteina.set('');
    this.formAcompanhamento.set('');
    this.formQuantidade.set('');
    this.formBebida.set('');
  }
}
