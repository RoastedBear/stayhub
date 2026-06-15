package com.roastedbear.stayhub.domain.accommodation.repository;

import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 숙소 레포지토리
 */
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    // 호스트가 등록한 숙소 목록 (페이징)
    Page<Accommodation> findByHostId(Long hostId, Pageable pageable);

    // 호스트의 특정 상태 숙소 목록
    Page<Accommodation> findByHostIdAndStatus(Long hostId, AccommodationStatus status, Pageable pageable);
}
