package com.roastedbear.stayhub.domain.room.repository;

import com.roastedbear.stayhub.domain.room.entity.Amenity;
import com.roastedbear.stayhub.domain.room.entity.AmenityCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 편의시설 레포지토리
 */
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    // 카테고리별 편의시설 목록
    List<Amenity> findByCategoryOrderByNameAsc(AmenityCategory category);
}
