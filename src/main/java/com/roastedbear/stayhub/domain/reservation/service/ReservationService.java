package com.roastedbear.stayhub.domain.reservation.service;

import com.roastedbear.stayhub.domain.reservation.dto.ReservationCreateRequest;
import com.roastedbear.stayhub.domain.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 예약 서비스 인터페이스
 */
public interface ReservationService {

    /** 예약 생성 - Redis 분산 락으로 동시성 제어 */
    ReservationResponse createReservation(Long guestId, ReservationCreateRequest request);

    /** 예약 취소 - PENDING/CONFIRMED 상태만 가능 */
    void cancelReservation(Long guestId, Long reservationId);

    /** 내 예약 목록 조회 - 최신순 페이징 */
    Page<ReservationResponse> getMyReservations(Long guestId, Pageable pageable);
}
