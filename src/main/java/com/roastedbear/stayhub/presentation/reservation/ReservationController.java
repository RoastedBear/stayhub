package com.roastedbear.stayhub.presentation.reservation;

import com.roastedbear.stayhub.domain.reservation.dto.ReservationCreateRequest;
import com.roastedbear.stayhub.domain.reservation.dto.ReservationResponse;
import com.roastedbear.stayhub.domain.reservation.service.ReservationService;
import com.roastedbear.stayhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예약 API 컨트롤러 (전체 인증 필요)
 *
 * POST   /api/reservations         - 예약 생성
 * PATCH  /api/reservations/{id}/cancel - 예약 취소
 * GET    /api/reservations/my      - 내 예약 목록
 */
@Tag(name = "Reservation", description = "예약 API")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 생성
     * POST /api/reservations
     *
     * - Redis 분산 락: lock:reservation:room:{roomId}
     * - 날짜 겹침 검증: PENDING + CONFIRMED 예약 대상
     * - 총 금액 = basePrice × 박수
     */
    @Operation(
            summary = "예약 생성",
            description = """
                    객실을 예약합니다.
                    - Redis 분산 락으로 동일 객실 동시 예약 직렬화
                    - DB 날짜 겹침 이중 검증
                    - 총 금액 = 1박 기본 가격 × 숙박 일수
                    """
    )
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse response = reservationService.createReservation(
                userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 예약 취소
     * PATCH /api/reservations/{id}/cancel
     *
     * - PENDING 또는 CONFIRMED 상태만 취소 가능
     * - 타인 예약 접근 시 404 (정보 노출 방지)
     */
    @Operation(
            summary = "예약 취소",
            description = "예약을 취소합니다. PENDING 또는 CONFIRMED 상태만 취소 가능합니다."
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "예약 ID") @PathVariable Long id) {
        reservationService.cancelReservation(userDetails.getMemberId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 예약 목록 조회
     * GET /api/reservations/my
     *
     * - 최신순 정렬
     * - 기본 10건 페이징
     */
    @Operation(summary = "내 예약 목록", description = "본인의 전체 예약 목록을 최신순으로 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<Page<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                reservationService.getMyReservations(userDetails.getMemberId(), pageable));
    }
}
