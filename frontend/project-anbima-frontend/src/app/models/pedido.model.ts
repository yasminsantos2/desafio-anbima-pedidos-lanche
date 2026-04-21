export enum StatusPedido {
  RECEBIDO = 'RECEBIDO',
  EM_PREPARACAO = 'EM_PREPARACAO',
  ENTREGUE = 'ENTREGUE'
}

export interface Pedido {
  id: number;
  tipoLanche: string;
  proteina: string;
  acompanhamento: string;
  quantidade: number;
  bebida: string;
  valor: number;
  status: StatusPedido;
  criadoEm: string;
}
