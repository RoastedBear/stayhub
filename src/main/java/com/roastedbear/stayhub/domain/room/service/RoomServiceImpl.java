package com.roastedbear.stayhub.domain.room.service;

import com.roastedbear.stayhub.domain.room.dto.RoomResponse;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchCondition;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchResponse;
import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.domain.room.repository.RoomRepository;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 객실 검색/조회 서비스 구현체
 * - QueryDSL RoomRepositoryImpl을 통해 동적 조건 검색 위임
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    /**
     * 객실 검색 (QueryDSL 동적 검색, 페이징)
     * - 조건: 지역(sido/sigungu), 날짜 가용성, 인원, 가격 범위
     * - 날짜 조건: checkIn + checkOut 모두 있어야 가용성 필터 적용
     * - 정렬: basePrice 오름차순
     */
    @Override
    public Page<RoomSearchResponse> searchRooms(RoomSearchCondition condition, Pageable pageable) {
        return roomRepository.searchAvailableRooms(condition, pageable)
                .map(RoomSearchResponse::from);
    }

    /**
     * 객실 상세 조회
     */
    @Override
    public RoomResponse getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return RoomResponse.from(room);
    }
}
