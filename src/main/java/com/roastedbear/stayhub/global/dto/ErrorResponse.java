package com.roastedbear.stayhub.global.dto;

import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

/**
 * API 에러 응답 공통 DTO
 */
@Getter
@Builder
public class ErrorResponse {

    private final String code;
    private final String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }
}