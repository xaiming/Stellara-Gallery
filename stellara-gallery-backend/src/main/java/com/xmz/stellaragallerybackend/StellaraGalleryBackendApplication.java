package com.xmz.stellaragallerybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.xmz.stellaragallerybackend.mapper")
@SpringBootApplication
/**
 * 星璃云图后端应用启动类。
 */
public class StellaraGalleryBackendApplication {

    /**
     * Spring Boot 应用入口。
     */
    public static void main(String[] args) {
        SpringApplication.run(StellaraGalleryBackendApplication.class, args);
    }

}
