package com.wireshark.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniWiresharkApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniWireshark API")
                        .description("API REST pour la capture de paquets réseau")
                        .version("1.0"));
    }
}
