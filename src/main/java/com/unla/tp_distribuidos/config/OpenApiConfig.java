package com.unla.tp_distribuidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rentarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentar API - Sistemas Distribuidos UNLa")
                        .description("Documentación Swagger/OpenAPI de los endpoints REST del sistema de alquiler de vehículos Rentar.")
                        .version("v1.0")
                        .contact(new Contact().name("UNLa Sistemas Distribuidos")));
    }
}
