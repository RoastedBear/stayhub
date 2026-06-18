package com.roastedbear.stayhub.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(springdoc-openapi) 설정
 * - JWT 인증 스키마 등록 → Swagger UI에서 Authorize 버튼 활성화
 * - @SecurityRequirement(name = "JWT")로 API별 인증 표시
 */
@OpenAPIDefinition(
        info = @Info(
                title = "StayHub API",
                description = "숙박 예약 플랫폼 StayHub REST API 문서",
                version = "v1"
        )
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "로그인 후 발급된 Access Token을 입력하세요. (Bearer 접두사 제외)"
)
@Configuration
public class SwaggerConfig {
}
