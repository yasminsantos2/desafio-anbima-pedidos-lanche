package com.anbima.lanches.infra.springdoc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfigurations {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Desafio ANBIMA - API de Pedidos de Lanche")
                        .description("API para gestão de pedidos de lanche, incluindo processamento de strings posicionais e integração com RabbitMQ. " +
                                     "Criada para o desafio técnico de backend.")
                        .version("1.0.0"));
    }
}
