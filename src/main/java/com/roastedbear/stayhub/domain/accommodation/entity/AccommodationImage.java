package com.roastedbear.stayhub.domain.accommodation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 숙소 이미지 엔티티
 * - S3에 업로드된 이미지 URL을 저장
 * - isThumbnail=true인 이미지가 대표 이미지로 사용됨
 */
@Entity
@Table(name = "accommodation_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccommodationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이미지가 속한 숙소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    // S3 이미지 URL
    @Column(nullable = false)
    private String imageUrl;

    // 대표 이미지 여부
    @Column(nullable = false)
    private boolean isThumbnail = false;

    // 이미지 정렬 순서
    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public AccommodationImage(Accommodation accommodation, String imageUrl,
                              boolean isThumbnail, Integer sortOrder) {
        this.accommodation = accommodation;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    /** 대표 이미지로 지정 */
    public void setAsThumbnail() {
        this.isThumbnail = true;
    }

    /** 대표 이미지 해제 */
    public void unsetThumbnail() {
        this.isThumbnail = false;
    }
}
