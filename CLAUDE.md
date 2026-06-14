# stayhub 프로젝트 규칙

## 프로젝트 정보
- **프로젝트명**: stayhub
- **도메인**: 숙박 예약 플랫폼
- **아키텍처**: Spring Boot 모놀리식
- **패키지**: com.roastedbear.stayhub

## 기술 스택
- **언어**: Java 17
- **프레임워크**: Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- **빌드 툴**: Gradle (Groovy DSL)
- **DB**: MySQL, Redis
- **검색/조회**: QueryDSL
- **인증**: JWT
- **결제**: Toss Payments
- **스토리지**: AWS S3
- **프론트**: Vue 3
- **API 문서**: Swagger (springdoc-openapi)
- **테스트**: JUnit5, Mockito
- **OS**: Windows

## 아키텍처 규칙
- DDD 기반 패키지 구조 (domain / application / infrastructure / presentation)
- Controller → Service → Repository 계층 명확히 구분
- 서비스 계층은 인터페이스 + 구현체 분리
- 예외는 커스텀 예외 클래스 사용 (BusinessException 등)
- `@Transactional` 범위는 최소한으로

## 코드 스타일
- 모든 코드는 한국어 주석 포함
- 변수/메서드명은 영어로
- Gradle 의존성 추가 시 build.gradle 기준으로 작성

## 응답 규칙
- 모든 답변은 한국어로
- 코드 수정 시 변경된 부분과 이유 설명
- 여러 방법이 있을 경우 트레이드오프 포함
- 코드 작성 후 커밋 메시지 추천 (feat / fix / refactor / chore)

## 주의사항
- 객실 예약 동시성 제어는 Redis 분산 락 사용
- 트랜잭션 경계 항상 명시적으로 설계