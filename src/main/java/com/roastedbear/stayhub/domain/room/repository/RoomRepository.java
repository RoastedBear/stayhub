package com.roastedbear.stayhub.domain.room.repository;

import com.roastedbear.stayhub.domain.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 객실 레포지토리
 * - JpaRepository: 기본 CRUD
 * - RoomRepositoryCustom: QueryDSL 동적 검색
 */
public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom {

    // 숙소에 속한 객실 목록 (페이징)
    Page<Room> findByAccommodationId(Long accommodationId, Pageable pageable);
}
