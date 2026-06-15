package com.roastedbear.stayhub.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 이미지 엔티티
 * - 게스트가 리뷰 작성 시 첨부하는 이미지 (S3 URL)
 */
@Entity
@Table(name = "review_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이미지가 속한 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // S3 이미지 URL
    @Column(nullable = false)
    private String imageUrl;

    // 이미지 정렬 순서
    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public ReviewImage(Review review, String imageUrl, Integer sortOrder) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
