package com.roastedbear.stayhub.presentation.review;

import com.roastedbear.stayhub.domain.review.service.ReviewImageService;
import com.roastedbear.stayhub.global.dto.ImageUploadResponse;
import com.roastedbear.stayhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 리뷰 이미지 API 컨트롤러 (리뷰 작성자 전용)
 *
 * POST   /api/reviews/{reviewId}/images           - 이미지 업로드
 * DELETE /api/reviews/{reviewId}/images/{imageId} - 이미지 삭제
 */
@Tag(name = "Review Image", description = "리뷰 이미지 API (리뷰 작성자 전용)")
@RestController
@RequestMapping("/api/reviews/{reviewId}/images")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class ReviewImageController {

    private final ReviewImageService reviewImageService;

    /**
     * 리뷰 이미지 업로드
     * POST /api/reviews/{reviewId}/images
     *
     * - 기존 리뷰에 이미지를 추가 업로드 (기존 이미지 유지)
     * - 리뷰 작성자 본인만 가능
     */
    @Operation(
            summary = "리뷰 이미지 업로드",
            description = """
                    기존 리뷰에 이미지를 추가 업로드합니다. (리뷰 작성자 전용)
                    - 기존 이미지는 유지되고 새 이미지가 추가됩니다.
                    - 한 번에 최대 10장까지 업로드 가능합니다.
                    - 허용 파일 형식: jpg, png, gif, webp / 최대 10MB
                    """
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageUploadResponse>> uploadImages(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @Parameter(description = "이미지 파일 목록 (최대 10장)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ImageUploadResponse> responses = reviewImageService.uploadImages(
                reviewId, userDetails.getMemberId(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * 리뷰 이미지 삭제
     * DELETE /api/reviews/{reviewId}/images/{imageId}
     *
     * - S3 파일 삭제 + DB 레코드 삭제
     * - 리뷰 작성자 본인만 가능
     */
    @Operation(
            summary = "리뷰 이미지 삭제",
            description = "리뷰 이미지를 삭제합니다. S3 파일과 DB 레코드가 함께 삭제됩니다. (리뷰 작성자 전용)"
    )
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @Parameter(description = "이미지 ID") @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        reviewImageService.deleteImage(reviewId, imageId, userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
