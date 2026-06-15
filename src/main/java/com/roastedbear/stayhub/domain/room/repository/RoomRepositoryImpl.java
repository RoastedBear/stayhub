package com.roastedbear.stayhub.domain.room.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationStatus;
import com.roastedbear.stayhub.domain.accommodation.entity.QAccommodation;
import com.roastedbear.stayhub.domain.reservation.entity.QReservation;
import com.roastedbear.stayhub.domain.reservation.entity.ReservationStatus;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchCondition;
import com.roastedbear.stayhub.domain.room.entity.QRoom;
import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.domain.room.entity.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Room QueryDSL 동적 검색 구현체
 *
 * 검색 조건 (모두 Optional - null이면 해당 조건 skip):
 *   1. 지역: sido eq / sigungu eq
 *   2. 인원: room.maxOccupancy >= guestCount
 *   3. 가격: room.basePrice between minPrice ~ maxPrice
 *   4. 날짜 가용성: 체크인~체크아웃 기간에 겹치는 PENDING/CONFIRMED 예약이 없는 객실만
 *      → NOT IN (서브쿼리: 겹치는 예약이 있는 room_id 목록)
 *
 * 날짜 겹침 공식:
 *   기존예약.checkIn < 요청.checkOut AND 기존예약.checkOut > 요청.checkIn
 */
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Room> searchAvailableRooms(RoomSearchCondition condition, Pageable pageable) {

        QRoom room = QRoom.room;
        QAccommodation accommodation = QAccommodation.accommodation;
        QReservation reservation = QReservation.reservation;

        // 동적 조건 조합
        BooleanBuilder builder = buildCondition(condition, room, accommodation, reservation);

        // 데이터 조회 쿼리 (accommodation fetchJoin → N+1 방지)
        List<Room> rooms = queryFactory
                .selectFrom(room)
                .join(room.accommodation, accommodation).fetchJoin()
                .where(builder)
                .orderBy(room.basePrice.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리 (fetchJoin 없이 분리 - 성능 최적화)
        JPAQuery<Long> countQuery = queryFactory
                .select(room.count())
                .from(room)
                .join(room.accommodation, accommodation)
                .where(builder);

        // count 쿼리는 마지막 페이지에서 결과가 pageSize보다 작으면 실행 생략됨
        return PageableExecutionUtils.getPage(rooms, pageable, countQuery::fetchOne);
    }

    /**
     * 검색 조건 BooleanBuilder 조합
     * - 각 조건은 null 체크 후 독립적으로 추가
     */
    private BooleanBuilder buildCondition(RoomSearchCondition condition,
                                          QRoom room,
                                          QAccommodation accommodation,
                                          QReservation reservation) {
        BooleanBuilder builder = new BooleanBuilder();

        // 운영중인 숙소 + 예약 가능 객실 기본 조건 (항상 적용)
        builder.and(accommodation.status.eq(AccommodationStatus.ACTIVE));
        builder.and(room.status.eq(RoomStatus.AVAILABLE));

        // 시/도 필터
        if (StringUtils.hasText(condition.getSido())) {
            builder.and(accommodation.sido.eq(condition.getSido()));
        }

        // 시/군/구 필터
        if (StringUtils.hasText(condition.getSigungu())) {
            builder.and(accommodation.sigungu.eq(condition.getSigungu()));
        }

        // 인원 필터 (최대 수용 인원 >= 요청 인원)
        if (condition.getGuestCount() != null) {
            builder.and(room.maxOccupancy.goe(condition.getGuestCount()));
        }

        // 최소 가격 필터
        if (condition.getMinPrice() != null) {
            builder.and(room.basePrice.goe(condition.getMinPrice()));
        }

        // 최대 가격 필터
        if (condition.getMaxPrice() != null) {
            builder.and(room.basePrice.loe(condition.getMaxPrice()));
        }

        // 날짜 가용성 필터 (체크인/체크아웃 둘 다 있을 때만 적용)
        if (condition.getCheckInDate() != null && condition.getCheckOutDate() != null) {
            // 날짜가 겹치는 예약이 있는 room_id 서브쿼리
            // 겹침 조건: 기존예약.checkIn < 요청.checkOut AND 기존예약.checkOut > 요청.checkIn
            builder.and(
                    room.id.notIn(
                            JPAExpressions
                                    .select(reservation.room.id)
                                    .from(reservation)
                                    .where(
                                            reservation.status.notIn(ReservationStatus.CANCELLED),
                                            reservation.checkInDate.lt(condition.getCheckOutDate()),
                                            reservation.checkOutDate.gt(condition.getCheckInDate())
                                    )
                    )
            );
        }

        return builder;
    }
}
