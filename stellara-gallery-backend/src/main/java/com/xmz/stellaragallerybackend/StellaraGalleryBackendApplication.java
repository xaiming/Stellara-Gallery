package com.xmz.stellaragallerybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.xmz.stellaragallerybackend.mapper")
@SpringBootApplication
public class StellaraGalleryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StellaraGalleryBackendApplication.class, args);
    }

}
