package com.roastedbear.stayhub.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드
 * - BusinessException과 함께 사용
 * - httpStatus: HTTP 응답 코드
 * - message: 클라이언트에 반환되는 메시지
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // === 회원 ===
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    SUSPENDED_ACCOUNT(HttpStatus.FORBIDDEN, "정지된 계정입니다."),
    DELETED_ACCOUNT(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다."),
    NOT_HOST(HttpStatus.FORBIDDEN, "호스트 권한이 필요합니다."),

    // === JWT ===
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token을 찾을 수 없습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다."),

    // === 숙소 ===
    ACCOMMODATION_NOT_FOUND(HttpStatus.NOT_FOUND, "숙소를 찾을 수 없습니다."),
    NOT_ACCOMMODATION_HOST(HttpStatus.FORBIDDEN, "해당 숙소의 호스트가 아닙니다."),

    // === 객실 ===
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "객실을 찾을 수 없습니다."),

    // === 예약 ===
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    ROOM_NOT_AVAILABLE(HttpStatus.CONFLICT, "선택한 날짜에 예약이 불가능한 객실입니다."),
    RESERVATION_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "취소할 수 없는 예약 상태입니다."),

    // === 결제 ===
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),

    // === 리뷰 ===
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 리뷰를 작성하였습니다."),
    REVIEW_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "이용 완료된 예약에만 리뷰를 작성할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
