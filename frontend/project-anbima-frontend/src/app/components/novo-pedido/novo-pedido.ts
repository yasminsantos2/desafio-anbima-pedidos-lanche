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
  
  protected payload = signal('');
  protected result = signal<Pedido | null>(null);
  protected error = signal<string | null>(null);
  protected loading = signal(false);

  validar(text: string): string[] {
    const errors: string[] = [];

    if (text.length !== 40) {
      errors.push(`O payload deve ter exatamente 40 caracteres (atual: ${text.length}).`);
      return errors;
    }

    const tipoLanche = text.substring(0, 10);
    const proteina = text.substring(10, 20);
    const acompanhamento = text.substring(20, 30);
    const quantidadeStr = text.substring(30, 32);
    const bebida = text.substring(32, 40);

    const isAlpha = (val: string) => /^[A-Z ]+$/.test(val);
    const isNumeric = (val: string) => /^\d+$/.test(val);

    if (!isAlpha(tipoLanche)) errors.push('Tipo Lanche deve conter apenas letras maiúsculas e espaços.');
    if (!isAlpha(proteina)) errors.push('Proteína deve conter apenas letras maiúsculas e espaços.');
    if (!isAlpha(acompanhamento)) errors.push('Acompanhamento deve conter apenas letras maiúsculas e espaços.');
    if (!isAlpha(bebida)) errors.push('Bebida deve conter apenas letras maiúsculas e espaços.');

    if (!isNumeric(quantidadeStr)) {
      errors.push('Quantidade deve conter apenas números.');
    } else {
      const qtd = parseInt(quantidadeStr, 10);
      if (qtd < 1 || qtd > 99) {
        errors.push('Quantidade deve estar entre 01 e 99.');
      }
    }

    return errors;
  }

  enviar() {
    const text = this.payload(); // Remove trim() to keep exact 40 chars if spaces are at ends
    const validationErrors = this.validar(text);

    if (validationErrors.length > 0) {
      this.error.set(validationErrors.join(' '));
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
        this.payload.set(''); // Opcional: limpa o campo após o sucesso
      },
      error: (err) => {
        console.error('Erro ao enviar pedido:', err);
        let errorMessage = 'Falha na comunicação com o servidor.';
        
        if (err.status === 0) {
          errorMessage = 'Não foi possível conectar ao servidor (Verifique se o backend está rodando na porta 8080).';
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
}
