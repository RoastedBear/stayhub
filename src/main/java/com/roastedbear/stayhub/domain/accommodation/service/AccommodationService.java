package com.roastedbear.stayhub.domain.accommodation.service;

import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationCreateRequest;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationResponse;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationSummaryResponse;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationUpdateRequest;
import com.roastedbear.stayhub.domain.room.dto.RoomCreateRequest;
import com.roastedbear.stayhub.domain.room.dto.RoomResponse;
import com.roastedbear.stayhub.domain.room.dto.RoomUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 숙소 관리 서비스 인터페이스
 * - 숙소 CRUD (호스트 권한 검증 포함)
 * - 객실 CRUD (해당 숙소 호스트 권한 검증 포함)
 */
public interface AccommodationService {

    AccommodationResponse createAccommodation(Long hostId, AccommodationCreateRequest request);

    AccommodationResponse getAccommodation(Long accommodationId);

    Page<AccommodationSummaryResponse> getMyAccommodations(Long hostId, Pageable pageable);

    AccommodationResponse updateAccommodation(Long hostId, Long accommodationId, AccommodationUpdateRequest request);

    void deleteAccommodation(Long hostId, Long accommodationId);

    RoomResponse createRoom(Long hostId, Long accommodationId, RoomCreateRequest request);

    RoomResponse updateRoom(Long hostId, Long roomId, RoomUpdateRequest request);

    void deleteRoom(Long hostId, Long roomId);

    Page<RoomResponse> getRooms(Long accommodationId, Pageable pageable);
}
