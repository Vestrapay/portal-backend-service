package com.example.vestrapay;

import com.example.vestrapay.utils.file_upload.IFileServiceImpl;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@SpringBootApplication
@EnableR2dbcAuditing
@OpenAPIDefinition(info = @Info(title = "VestraPay Payment Gateway Service", version = "v1.0.0"))
@SecurityScheme(name = "vestrapay", scheme = "Bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class VestraPayApplication implements CommandLineRunner {
    @Resource
    IFileServiceImpl fileService;
    public static void main(String[] args) {
        SpringApplication.run(VestraPayApplication.class, args);
    }

    @Override
    public void run(String... arg) throws Exception {
        fileService.init();
    }



}
