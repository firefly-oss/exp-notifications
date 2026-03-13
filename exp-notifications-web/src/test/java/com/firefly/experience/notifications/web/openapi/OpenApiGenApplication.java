package com.firefly.experience.notifications.web.openapi;

import org.fireflyframework.web.openapi.EnableOpenApiGen;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Lightweight Spring Boot application used exclusively during the Maven
 * {@code generate-openapi} profile to expose the OpenAPI spec at
 * {@code http://localhost:18080/v3/api-docs.yaml} for SDK generation.
 */
@EnableOpenApiGen
@ComponentScan(basePackages = "com.firefly.experience.notifications.web.controllers")
public class OpenApiGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiGenApplication.class, args);
    }
}
