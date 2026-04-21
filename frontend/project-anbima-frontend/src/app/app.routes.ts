import { Routes } from '@angular/router';
import { NovoPedidoComponent } from './components/novo-pedido/novo-pedido';
import { PedidosComponent } from './components/pedidos/pedidos';

export const routes: Routes = [
  { path: '', redirectTo: 'novo-pedido', pathMatch: 'full' },
  { path: 'novo-pedido', component: NovoPedidoComponent },
  { path: 'listagem', component: PedidosComponent }
];
