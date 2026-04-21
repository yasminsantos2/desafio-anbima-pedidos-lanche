import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pedido } from '../models/pedido.model';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/pedidos';

  listarPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.apiUrl);
  }

  enviarPedidoPosicional(payload: string): Observable<Pedido> {
    const headers = new HttpHeaders({ 'Content-Type': 'text/plain' });
    return this.http.post<Pedido>(`${this.apiUrl}/posicional`, payload, { headers });
  }

  atualizarStatus(id: number, status: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.apiUrl}/${id}/status`, { status });
  }

  processarFila(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/processar-fila`, {});
  }
}
