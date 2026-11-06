package com.medical.medicine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medicine Caching API (Redis Cache)")
                        .version("1.0.0")
                        .description("API quan ly va caching danh muc thuoc su dung Spring Data Redis theo Cache-aside pattern.")
                        .contact(new Contact()
                                .name("Medical Backend Team")
                                .email("contact@medical.local")
                        )
                );
    }
}
