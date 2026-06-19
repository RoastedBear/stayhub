package com.roastedbear.stayhub.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Toss Payments WebClient 설정
 * - 시크릿 키를 Basic 인증으로 인코딩하여 기본 헤더에 설정
 * - Authorization: Basic Base64(secretKey:)
 */
@Configuration
public class TossPaymentConfig {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.base-url:https://api.tosspayments.com}")
    private String baseUrl;

    @Bean
    public WebClient tossWebClient() {
        // Toss Basic 인증: Base64(secretKey + ":")
        String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
