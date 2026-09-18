
package com.integration.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI integrationMonitorOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Integration Monitor API")
                        .version("1.0.0")
                        .description(
                                "REST API for monitoring and managing " +
                                "enterprise integration processes."
                        )
                        .contact(new Contact()
                                .name("Integration Monitor Team")
                        )
                );
    }
}

