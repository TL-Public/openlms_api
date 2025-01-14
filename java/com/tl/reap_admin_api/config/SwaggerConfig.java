package com.tl.reap_admin_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        try {
            return new OpenAPI()
                .info(new Info().title("REAP APIS").version("1.0").description("API documentation"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
       
    }
}