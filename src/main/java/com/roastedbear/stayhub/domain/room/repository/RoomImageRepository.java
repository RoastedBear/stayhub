package com.roastedbear.stayhub.domain.room.repository;

import com.roastedbear.stayhub.domain.room.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 객실 이미지 레포지토리
 */
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    // 객실 이미지 목록 (정렬 순서대로)
    List<RoomImage> findByRoomIdOrderBySortOrderAsc(Long roomId);

    // 객실 대표 이미지 조회
    Optional<RoomImage> findByRoomIdAndIsThumbnailTrue(Long roomId);

    // 객실 이미지 전체 삭제 (객실 삭제 시)
    void deleteByRoomId(Long roomId);
}
