package com.cce.codeeditor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Collaborative Code Editor API")
                        .version("1.0.0")
                        .description("Real-time collaborative code editor with WebSocket synchronization and sandboxed code execution")
                        .contact(new Contact()
                                .name("Mohit Prasad")
                                .url("https://github.com/mhtpsd"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
