package com.company.approval_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

//Swagger config for interactive API docs.
@Configuration
public class SwaggerConfig {

    public OpenAPI customAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Internal Approval System API")
                        .version("1.0.0")
                        .description("Production-ready API for internal approval and request management system")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@compant.com"))
                        .license(new License()
                                .name("Internal Use Only")
                                .url("https:/company.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/v1/auth/login")));
    }

}
