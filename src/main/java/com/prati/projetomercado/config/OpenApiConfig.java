package com.prati.projetomercado.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration // Marca esta classe como uma classe de configuração do Spring
@OpenAPIDefinition(info = @Info(title = "Projeto Mercado API", version = "v1")) // Define informações gerais da API
@SecurityScheme(
        name = "bearerAuth", // Este é o nome que usamos na anotação @SecurityRequirement
        type = SecuritySchemeType.HTTP, // O tipo de esquema é HTTP
        bearerFormat = "JWT", // O formato do token
        scheme = "bearer" // O esquema de autenticação é "bearer"
)
public class OpenApiConfig {
    // Esta classe não precisa de nenhum método, apenas das anotações acima.
}