package com.roastedbear.stayhub.domain.reservation.repository;

import com.roastedbear.stayhub.domain.reservation.entity.Reservation;
import com.roastedbear.stayhub.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 레포지토리
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 날짜 겹침 예약 존재 여부 확인 (가용성 체크)
     *
     * Redis 분산 락과 함께 사용: 락 획득 후 이 쿼리로 최종 검증
     *
     * 겹침 조건: 기존예약.checkIn < 요청.checkOut AND 기존예약.checkOut > 요청.checkIn
     * - 취소된 예약은 제외
     *
     * @param roomId       대상 객실 ID
     * @param checkInDate  요청 체크인 날짜
     * @param checkOutDate 요청 체크아웃 날짜
     * @return 겹치는 예약 존재 여부
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.room.id = :roomId
              AND r.status != :cancelledStatus
              AND r.checkInDate < :checkOutDate
              AND r.checkOutDate > :checkInDate
            """)
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("cancelledStatus") ReservationStatus cancelledStatus
    );

    // 게스트 예약 목록 (최신순, 페이징)
    Page<Reservation> findByGuestIdOrderByCreatedAtDesc(Long guestId, Pageable pageable);

    // 특정 객실의 확정된 예약 목록 (날짜 범위 - 캘린더 표시용)
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.room.id = :roomId
              AND r.status IN :statuses
              AND r.checkOutDate > :from
              AND r.checkInDate < :to
            """)
    List<Reservation> findByRoomIdAndStatusInAndDateRange(
            @Param("roomId") Long roomId,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 호스트 숙소의 예약 목록 (호스트 대시보드용)
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.room.accommodation.host.id = :hostId
            ORDER BY r.createdAt DESC
            """)
    Page<Reservation> findByHostId(@Param("hostId") Long hostId, Pageable pageable);
}
