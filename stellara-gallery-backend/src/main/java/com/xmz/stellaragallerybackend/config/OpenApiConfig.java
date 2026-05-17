package com.xmz.stellaragallerybackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stellaraGalleryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stellara Gallery API")
                        .description("Stellara Gallery 后端接口文档")
                        .version("1.0.0"));
    }
}
