package com.roastedbear.stayhub.presentation.room;

import com.roastedbear.stayhub.domain.room.service.RoomImageService;
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
 * 객실 이미지 API 컨트롤러 (호스트 전용)
 *
 * POST   /api/rooms/{id}/images                    - 이미지 업로드
 * DELETE /api/rooms/{id}/images/{imageId}          - 이미지 삭제
 * PATCH  /api/rooms/{id}/images/{imageId}/thumbnail - 대표 이미지 변경
 */
@Tag(name = "Room Image", description = "객실 이미지 API (호스트 전용)")
@RestController
@RequestMapping("/api/rooms/{roomId}/images")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class RoomImageController {

    private final RoomImageService roomImageService;

    @Operation(
            summary = "객실 이미지 업로드",
            description = """
                    객실 이미지를 업로드합니다. (호스트 전용)
                    - 한 번에 최대 10장까지 업로드 가능합니다.
                    - 허용 파일 형식: jpg, png, gif, webp / 최대 10MB
                    """
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageUploadResponse>> uploadImages(
            @Parameter(description = "객실 ID") @PathVariable Long roomId,
            @Parameter(description = "이미지 파일 목록 (최대 10장)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ImageUploadResponse> responses = roomImageService.uploadImages(
                roomId, userDetails.getMemberId(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(
            summary = "객실 이미지 삭제",
            description = "객실 이미지를 삭제합니다. S3 파일과 DB 레코드가 함께 삭제됩니다. (호스트 전용)"
    )
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "객실 ID") @PathVariable Long roomId,
            @Parameter(description = "이미지 ID") @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        roomImageService.deleteImage(roomId, imageId, userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "객실 대표 이미지 변경",
            description = "지정한 이미지를 객실 대표 이미지로 변경합니다. (호스트 전용)"
    )
    @PatchMapping("/{imageId}/thumbnail")
    public ResponseEntity<ImageUploadResponse> setThumbnail(
            @Parameter(description = "객실 ID") @PathVariable Long roomId,
            @Parameter(description = "이미지 ID") @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ImageUploadResponse response = roomImageService.setThumbnail(
                roomId, imageId, userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }
}
