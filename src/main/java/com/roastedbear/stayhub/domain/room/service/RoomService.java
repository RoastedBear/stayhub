package com.roastedbear.stayhub.domain.room.service;

import com.roastedbear.stayhub.domain.room.dto.RoomResponse;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchCondition;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 객실 검색/조회 서비스 인터페이스
 */
public interface RoomService {

    /** QueryDSL 동적 검색 - 모든 조건 Optional */
    Page<RoomSearchResponse> searchRooms(RoomSearchCondition condition, Pageable pageable);

    /** 객실 상세 조회 */
    RoomResponse getRoom(Long roomId);
}
