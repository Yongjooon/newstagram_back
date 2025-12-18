package com.ssafy.newstagram.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "NEWSTAGRAM API 명세서", description = "Newstagram API 명세서입니다."),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {

    @Bean
    GroupedOpenApi userOpenApi(){
        String[] paths = {"/users/**", "/auth/**", "/oauth2/**"};
        return GroupedOpenApi.builder().group("User & Auth 관련 API")
                                        .pathsToMatch(paths)
                                        .build();
    }

    @Bean
    GroupedOpenApi articleOpenApi(){
        String[] paths = {"/article/**", "/v1/search/**"};
        return GroupedOpenApi.builder().group("Article 관련 API")
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    GroupedOpenApi loggingOpenApi(){
        String[] paths = {"/logging/**"};
        return GroupedOpenApi.builder()
                .group("Kafka 발행 API")
                .pathsToMatch(paths)
                .build();
    }
}
