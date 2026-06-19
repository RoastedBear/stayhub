package com.roastedbear.stayhub.global.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.net.URI;

/**
 * AWS S3 / LocalStack 클라이언트 설정
 *
 * 로컬 개발 환경: LocalStack (endpoint=http://localhost:4566)
 * 운영 환경: 환경변수 S3_ENDPOINT, AWS_ACCESS_KEY, AWS_SECRET_KEY로 교체
 *
 * path-style-access: LocalStack은 가상 호스팅 방식 미지원 → 경로 방식 강제 활성화
 */
@Slf4j
@Configuration
public class S3Config {

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.aws.s3.region}")
    private String region;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // LocalStack 필수: 경로 방식 접근 (가상 호스팅 방식 미지원)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /**
     * 애플리케이션 시작 시 버킷이 없으면 자동 생성
     * - LocalStack 개발 환경용
     * - LocalStack이 실행 중이지 않으면 경고만 출력하고 계속 진행
     */
    @PostConstruct
    public void initBucket() {
        try {
            S3Client client = s3Client();
            try {
                client.headBucket(r -> r.bucket(bucketName));
                log.info("S3 버킷 확인 완료: {}", bucketName);
            } catch (NoSuchBucketException e) {
                client.createBucket(r -> r.bucket(bucketName));
                log.info("S3 버킷 생성 완료: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("S3 버킷 초기화 실패 - LocalStack이 실행 중인지 확인하세요: {}", e.getMessage());
        }
    }
}
