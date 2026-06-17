package com.roastedbear.stayhub.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 (서비스 계층에서 발생)
 * - ErrorCode를 통해 HTTP 상태 코드와 메시지를 정의
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
