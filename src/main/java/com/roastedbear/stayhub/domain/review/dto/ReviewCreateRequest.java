package com.roastedbear.stayhub.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 작성 요청 DTO
 * - COMPLETED 상태의 예약에 대해서만 작성 가능
 * - 이미지 URL은 현재 직접 입력 방식 (S3 연동 전)
 */
@Getter
@NoArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "예약 ID는 필수입니다.")
    private Long reservationId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1000자 이하로 작성해주세요.")
    private String content;

    /** 이미지 URL 목록 (선택, 최대 10장) */
    @Size(max = 10, message = "이미지는 최대 10장까지 첨부할 수 있습니다.")
    private List<String> imageUrls = new ArrayList<>();
}
