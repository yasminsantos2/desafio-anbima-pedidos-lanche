# Dockerfile para o Frontend Angular
FROM node:20-alpine

WORKDIR /app

# Copia arquivos de dependências
COPY package*.json ./

# Instala dependências
RUN npm install

# Copia o restante dos arquivos do projeto
COPY . .

# Expõe a porta do Angular
EXPOSE 4200

# Inicia o servidor do Angular permitindo conexões externas
# Usando apenas --host 0.0.0.0 para compatibilidade
CMD ["npm", "start", "--", "--host", "0.0.0.0"]
