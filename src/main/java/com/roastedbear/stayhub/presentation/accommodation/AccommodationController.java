package com.roastedbear.stayhub.presentation.accommodation;

import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationCreateRequest;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationResponse;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationSummaryResponse;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationUpdateRequest;
import com.roastedbear.stayhub.domain.accommodation.service.AccommodationService;
import com.roastedbear.stayhub.domain.room.dto.RoomCreateRequest;
import com.roastedbear.stayhub.domain.room.dto.RoomResponse;
import com.roastedbear.stayhub.domain.room.dto.RoomUpdateRequest;
import com.roastedbear.stayhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 숙소 관리 API 컨트롤러
 *
 * 공개 (인증 불필요):
 *   GET  /api/accommodations/{id}
 *   GET  /api/accommodations/{accommodationId}/rooms
 *
 * 인증 필요 (호스트):
 *   POST   /api/accommodations
 *   GET    /api/accommodations/my
 *   PUT    /api/accommodations/{id}
 *   DELETE /api/accommodations/{id}
 *   POST   /api/accommodations/{accommodationId}/rooms
 *   PUT    /api/accommodations/{accommodationId}/rooms/{roomId}
 *   DELETE /api/accommodations/{accommodationId}/rooms/{roomId}
 */
@Tag(name = "Accommodation", description = "숙소 관리 API")
@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    // ========== 숙소 CRUD ==========

    /**
     * 숙소 등록
     * POST /api/accommodations
     */
    @Operation(summary = "숙소 등록", description = "새 숙소를 등록합니다. 최초 등록 시 HOST 역할이 자동 부여됩니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccommodationResponse> createAccommodation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccommodationCreateRequest request) {
        AccommodationResponse response = accommodationService.createAccommodation(
                userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 숙소 목록 조회 (ACTIVE 상태만)
     * GET /api/accommodations/my
     *
     * 주의: SecurityConfig에서 GET /api/accommodations/** 는 permitAll이나
     *       @PreAuthorize로 인증 강제
     */
    @Operation(summary = "내 숙소 목록", description = "호스트 본인이 등록한 활성 숙소 목록을 조회합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AccommodationSummaryResponse>> getMyAccommodations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                accommodationService.getMyAccommodations(userDetails.getMemberId(), pageable));
    }

    /**
     * 숙소 상세 조회 (공개)
     * GET /api/accommodations/{id}
     */
    @Operation(summary = "숙소 상세 조회", description = "숙소 ID로 상세 정보를 조회합니다. (공개)")
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationResponse> getAccommodation(
            @Parameter(description = "숙소 ID") @PathVariable Long id) {
        return ResponseEntity.ok(accommodationService.getAccommodation(id));
    }

    /**
     * 숙소 수정 (호스트 본인만)
     * PUT /api/accommodations/{id}
     */
    @Operation(summary = "숙소 수정", description = "숙소 정보를 수정합니다. 호스트 본인만 가능합니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccommodationResponse> updateAccommodation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "숙소 ID") @PathVariable Long id,
            @Valid @RequestBody AccommodationUpdateRequest request) {
        AccommodationResponse response = accommodationService.updateAccommodation(
                userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 숙소 삭제 (소프트 삭제, 호스트 본인만)
     * DELETE /api/accommodations/{id}
     */
    @Operation(summary = "숙소 삭제", description = "숙소를 삭제합니다 (소프트 삭제). 호스트 본인만 가능합니다.")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccommodation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "숙소 ID") @PathVariable Long id) {
        accommodationService.deleteAccommodation(userDetails.getMemberId(), id);
        return ResponseEntity.noContent().build();
    }

    // ========== 객실 CRUD ==========

    /**
     * 객실 등록 (해당 숙소의 호스트만)
     * POST /api/accommodations/{accommodationId}/rooms
     */
    @Operation(summary = "객실 등록", description = "숙소에 새 객실을 등록합니다. 해당 숙소의 호스트만 가능합니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{accommodationId}/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoomResponse> createRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @Valid @RequestBody RoomCreateRequest request) {
        RoomResponse response = accommodationService.createRoom(
                userDetails.getMemberId(), accommodationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 숙소의 객실 목록 조회 (공개)
     * GET /api/accommodations/{accommodationId}/rooms
     */
    @Operation(summary = "객실 목록 조회", description = "숙소에 속한 전체 객실 목록을 조회합니다. (공개)")
    @GetMapping("/{accommodationId}/rooms")
    public ResponseEntity<Page<RoomResponse>> getRooms(
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(accommodationService.getRooms(accommodationId, pageable));
    }

    /**
     * 객실 수정 (해당 숙소의 호스트만)
     * PUT /api/accommodations/{accommodationId}/rooms/{roomId}
     */
    @Operation(summary = "객실 수정", description = "객실 정보를 수정합니다. 해당 숙소의 호스트만 가능합니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{accommodationId}/rooms/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoomResponse> updateRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @Parameter(description = "객실 ID") @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateRequest request) {
        return ResponseEntity.ok(
                accommodationService.updateRoom(userDetails.getMemberId(), roomId, request));
    }

    /**
     * 객실 삭제 (비활성화, 해당 숙소의 호스트만)
     * DELETE /api/accommodations/{accommodationId}/rooms/{roomId}
     */
    @Operation(summary = "객실 삭제", description = "객실을 비활성화합니다 (UNAVAILABLE). 해당 숙소의 호스트만 가능합니다.")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{accommodationId}/rooms/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "숙소 ID") @PathVariable Long accommodationId,
            @Parameter(description = "객실 ID") @PathVariable Long roomId) {
        accommodationService.deleteRoom(userDetails.getMemberId(), roomId);
        return ResponseEntity.noContent().build();
    }
}
