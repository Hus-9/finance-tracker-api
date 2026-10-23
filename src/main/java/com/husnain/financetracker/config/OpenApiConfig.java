package com.husnain.financetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// This class sets up the Swagger docs page for the API so you can see all the endpoints and test them in the browser.
// It also tells Swagger the API uses JWT tokens so you get an Authorize button where you paste your token to test the locked endpoints.
// @Configuration just tells Spring this class has setup stuff in it so Spring loads it when the app starts.

@Configuration

public class OpenApiConfig {

    // @Bean means Spring makes this OpenAPI object once and keeps it, then springdoc picks it up and uses it to build the docs page.
    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                // The title and version that show up at the top of the docs page.
                .info(new Info().title("Personal Finance Tracker API").version("1.0.0"))
                // Makes a security scheme called bearerAuth, basically saying the API wants a JWT sent as a Bearer token in the Authorization header.
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                // Puts bearerAuth on every endpoint by default so once you click Authorize and put your token in, Swagger sends it with every request.
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));


    }
}
