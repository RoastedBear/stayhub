package com.roastedbear.stayhub.presentation.accommodation;

import com.roastedbear.stayhub.domain.accommodation.service.AccommodationImageService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 숙소 이미지 API 컨트롤러 (호스트 전용)
 *
 * POST   /api/accommodations/{id}/images                    - 이미지 업로드 (멀티파트)
 * DELETE /api/accommodations/{id}/images/{imageId}          - 이미지 삭제
 * PATCH  /api/accommodations/{id}/images/{imageId}/thumbnail - 대표 이미지 변경
 */
@Tag(name = "Accommodation Image", description = "숙소 이미지 API (호스트 전용)")
@RestController
@RequestMapping("/api/accommodations/{accommodationId}/images")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class AccommodationImageController {

    private final AccommodationImageService accommodationImageService;

    /**
     * 숙소 이미지 업로드
     * POST /api/accommodations/{accommodationId}/images
     *
     * - 한 번에 최대 10장 업로드
     * - 허용 형식: jpg, png, gif, webp (최대 10MB/장)
     * - 숙소에 이미지가 없으면 첫 번째 이미지가 대표 이미지로 자동 지정
     */
    @Operation(
            summary = "숙소 이미지 업로드",
            description = """
                    숙소 이미지를 업로드합니다. (호스트 전용)
                    - 한 번에 최대 10장까지 업로드 가능합니다.
                    - 허용 파일 형식: jpg, png, gif, webp
                    - 최대 파일 크기: 10MB / 장
                    - 숙소에 이미지가 없으면 첫 번째 이미지가 대표 이미지로 자동 지정됩니다.
                    """
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageUploadResponse>> uploadImages(
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @Parameter(description = "이미지 파일 목록 (최대 10장)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ImageUploadResponse> responses = accommodationImageService.uploadImages(
                accommodationId, userDetails.getMemberId(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * 숙소 이미지 삭제
     * DELETE /api/accommodations/{accommodationId}/images/{imageId}
     *
     * - S3 파일 삭제 + DB 레코드 삭제
     * - 대표 이미지 삭제 시 다음 이미지가 대표 이미지로 자동 승격
     */
    @Operation(
            summary = "숙소 이미지 삭제",
            description = """
                    숙소 이미지를 삭제합니다. (호스트 전용)
                    - S3 파일과 DB 레코드가 함께 삭제됩니다.
                    - 대표 이미지 삭제 시 다음 이미지가 자동으로 대표 이미지로 승격됩니다.
                    """
    )
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @Parameter(description = "이미지 ID") @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        accommodationImageService.deleteImage(accommodationId, imageId, userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 대표 이미지 변경
     * PATCH /api/accommodations/{accommodationId}/images/{imageId}/thumbnail
     */
    @Operation(
            summary = "숙소 대표 이미지 변경",
            description = "지정한 이미지를 숙소 대표 이미지로 변경합니다. (호스트 전용)"
    )
    @PatchMapping("/{imageId}/thumbnail")
    public ResponseEntity<ImageUploadResponse> setThumbnail(
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @Parameter(description = "이미지 ID") @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ImageUploadResponse response = accommodationImageService.setThumbnail(
                accommodationId, imageId, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
}
