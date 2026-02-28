package com.bapsdelhibalmandal.balbalika_management_system.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc/OpenAPI Configuration
 * This ensures SpringDoc doesn't interfere with multipart/form-data requests
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bal Balika Management System API")
                        .version("1.0.0")
                        .description("API for managing Bal Balika attendance and data"));
    }
}
