package com.roastedbear.stayhub.presentation.room;

import com.roastedbear.stayhub.domain.room.dto.RoomResponse;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchCondition;
import com.roastedbear.stayhub.domain.room.dto.RoomSearchResponse;
import com.roastedbear.stayhub.domain.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 객실 검색/조회 API 컨트롤러 (공개 - 인증 불필요)
 *
 * SecurityConfig 설정:
 *   - GET /api/rooms/search → permitAll
 *   - GET /api/rooms/{id}   → permitAll
 */
@Tag(name = "Room", description = "객실 검색/조회 API")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 객실 검색 (QueryDSL 동적 검색)
     * GET /api/rooms/search
     *
     * 모든 파라미터 optional - null이면 해당 조건 무시
     * 날짜 두 값 모두 입력 시에만 가용성 필터 적용
     */
    @Operation(
            summary = "객실 검색",
            description = """
                    QueryDSL 동적 검색으로 조건에 맞는 객실을 검색합니다.
                    - 모든 조건은 선택사항 (미입력 시 해당 조건 무시)
                    - checkInDate + checkOutDate 모두 입력 시 날짜 가용성 체크
                    - 기본 정렬: 가격 오름차순
                    """
    )
    @GetMapping("/search")
    public ResponseEntity<Page<RoomSearchResponse>> searchRooms(
            @Parameter(description = "시/도 (예: 서울특별시)")
            @RequestParam(required = false) String sido,

            @Parameter(description = "시/군/구 (예: 강남구)")
            @RequestParam(required = false) String sigungu,

            @Parameter(description = "체크인 날짜 (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,

            @Parameter(description = "체크아웃 날짜 (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,

            @Parameter(description = "투숙 인원")
            @RequestParam(required = false) Integer guestCount,

            @Parameter(description = "최소 가격 (원)")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "최대 가격 (원)")
            @RequestParam(required = false) BigDecimal maxPrice,

            @PageableDefault(size = 10) Pageable pageable) {

        RoomSearchCondition condition = new RoomSearchCondition();
        condition.setSido(sido);
        condition.setSigungu(sigungu);
        condition.setCheckInDate(checkInDate);
        condition.setCheckOutDate(checkOutDate);
        condition.setGuestCount(guestCount);
        condition.setMinPrice(minPrice);
        condition.setMaxPrice(maxPrice);

        return ResponseEntity.ok(roomService.searchRooms(condition, pageable));
    }

    /**
     * 객실 상세 조회
     * GET /api/rooms/{id}
     */
    @Operation(summary = "객실 상세 조회", description = "객실 ID로 상세 정보 및 편의시설 목록을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(
            @Parameter(description = "객실 ID") @PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }
}
