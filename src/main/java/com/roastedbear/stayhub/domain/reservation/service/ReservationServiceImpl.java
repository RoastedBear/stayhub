package com.roastedbear.stayhub.domain.reservation.service;

import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.member.repository.MemberRepository;
import com.roastedbear.stayhub.domain.reservation.dto.ReservationCreateRequest;
import com.roastedbear.stayhub.domain.reservation.dto.ReservationResponse;
import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import com.roastedbear.stayhub.domain.reservation.entity.ReservationStatus;
import com.roastedbear.stayhub.domain.reservation.repository.ReservationRepository;
import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.domain.room.entity.RoomStatus;
import com.roastedbear.stayhub.domain.room.repository.RoomRepository;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import com.roastedbear.stayhub.global.lock.DistributedLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

/**
 * 예약 서비스 구현체
 *
 * 동시성 제어 전략 (이중 잠금):
 *   1. Redis 분산 락 (lock:reservation:room:{roomId}) - 동시 요청 직렬화
 *   2. DB 쿼리 (existsOverlappingReservation) - 날짜 겹침 최종 검증
 *
 * 날짜 겹침 공식: 기존.checkIn < 요청.checkOut AND 기존.checkOut > 요청.checkIn
 * 총 금액: basePrice × (checkOutDate - checkInDate 일수)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final String LOCK_KEY_PREFIX = "lock:reservation:room:";

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final DistributedLockService lockService;

    /**
     * 예약 생성
     * 1. 날짜 유효성 검증 (checkIn < checkOut, 과거 날짜 불가)
     * 2. Redis 분산 락 획득 (동일 객실 동시 요청 직렬화)
     * 3. 객실 상태 및 인원 확인
     * 4. DB 날짜 겹침 최종 검증
     * 5. 예약 생성 및 저장
     */
    @Override
    @Transactional
    public ReservationResponse createReservation(Long guestId, ReservationCreateRequest request) {
        validateDates(request);

        String lockKey = LOCK_KEY_PREFIX + request.getRoomId();

        return lockService.executeWithLock(lockKey, () -> {
            Member guest = memberRepository.findById(guestId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

            // 객실 예약 가능 상태 확인
            if (room.getStatus() != RoomStatus.AVAILABLE) {
                throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
            }

            // 최대 수용 인원 초과 확인
            if (request.getGuestCount() > room.getMaxOccupancy()) {
                throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
            }

            // DB 최종 날짜 겹침 검증 (락 내부에서 실행)
            boolean hasOverlap = reservationRepository.existsOverlappingReservation(
                    request.getRoomId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate(),
                    ReservationStatus.CANCELLED
            );

            if (hasOverlap) {
                throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
            }

            // 총 금액 = 기본 가격 × 박수
            long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
            BigDecimal totalPrice = room.getBasePrice().multiply(BigDecimal.valueOf(nights));

            Reservation reservation = Reservation.builder()
                    .room(room)
                    .guest(guest)
                    .checkInDate(request.getCheckInDate())
                    .checkOutDate(request.getCheckOutDate())
                    .guestCount(request.getGuestCount())
                    .totalPrice(totalPrice)
                    .build();

            return ReservationResponse.from(reservationRepository.save(reservation));
        });
    }

    /**
     * 예약 취소
     * - 본인 예약만 취소 가능 (타인 예약 접근 시 404 반환으로 정보 노출 방지)
     * - PENDING / CONFIRMED 상태만 취소 가능
     */
    @Override
    @Transactional
    public void cancelReservation(Long guestId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 타인 예약 접근 시 존재하지 않는 것처럼 처리 (정보 노출 방지)
        if (!reservation.getGuest().getId().equals(guestId)) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        if (!reservation.isCancellable()) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELLABLE);
        }

        reservation.cancel();
    }

    /**
     * 내 예약 목록 조회 (최신순, 페이징)
     */
    @Override
    public Page<ReservationResponse> getMyReservations(Long guestId, Pageable pageable) {
        return reservationRepository
                .findByGuestIdOrderByCreatedAtDesc(guestId, pageable)
                .map(ReservationResponse::from);
    }

    // 날짜 기본 유효성 검증
    private void validateDates(ReservationCreateRequest request) {
        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜 이후여야 합니다.");
        }
        if (request.getCheckInDate().isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")))) {
            throw new IllegalArgumentException("체크인 날짜는 오늘 이후여야 합니다.");
        }
    }
}
