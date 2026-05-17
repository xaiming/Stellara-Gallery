package com.xmz.stellaragallerybackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * OpenAPI / Swagger 文档配置。
 */
public class OpenApiConfig {

    /**
     * 配置 Swagger / OpenAPI 文档的基础信息。
     */
    @Bean
    public OpenAPI stellaraGalleryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stellara Gallery API")
                        .description("Stellara Gallery 后端接口文档")
                        .version("1.0.0"));
    }
}
