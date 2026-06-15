package com.roastedbear.stayhub.domain.room.repository;

import com.roastedbear.stayhub.domain.room.dto.RoomSearchCondition;
import com.roastedbear.stayhub.domain.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Room QueryDSL 동적 검색 커스텀 인터페이스
 */
public interface RoomRepositoryCustom {

    /**
     * 조건에 맞는 예약 가능 객실 검색 (페이징)
     *
     * @param condition 검색 조건 (지역, 날짜, 인원, 가격 - 모두 Optional)
     * @param pageable  페이징 정보
     * @return 조건을 만족하는 객실 목록 (Page)
     */
    Page<Room> searchAvailableRooms(RoomSearchCondition condition, Pageable pageable);
}
