CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    tipo_lanche VARCHAR(20),
    proteina VARCHAR(20),
    acompanhamento VARCHAR(20),
    quantidade INT,
    bebida VARCHAR(20),
    valor NUMERIC(10,2),
    status VARCHAR(20),
    criado_em TIMESTAMP DEFAULT now()
);
