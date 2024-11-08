package com.example.vestrapay;

import com.example.vestrapay.merchant.dispute.interfaces.IDisputeFileService;
import com.example.vestrapay.utils.file_upload.IFileService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableR2dbcAuditing
@OpenAPIDefinition(info = @Info(title = "VestraPay Payment Gateway Service", version = "v1.0.0"),servers = @Server(url = "/portal"))
@SecurityScheme(name = "vestrapay", scheme = "Bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class VestraPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(VestraPayApplication.class, args);
    }




}
