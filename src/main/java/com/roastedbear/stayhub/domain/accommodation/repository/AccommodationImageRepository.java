package com.roastedbear.stayhub.domain.accommodation.repository;

import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 숙소 이미지 레포지토리
 */
public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, Long> {

    // 숙소의 이미지 목록 (정렬 순서대로)
    List<AccommodationImage> findByAccommodationIdOrderBySortOrderAsc(Long accommodationId);

    // 숙소의 대표 이미지 조회
    Optional<AccommodationImage> findByAccommodationIdAndIsThumbnailTrue(Long accommodationId);

    // 숙소 이미지 전체 삭제 (숙소 삭제 시)
    void deleteByAccommodationId(Long accommodationId);
}
