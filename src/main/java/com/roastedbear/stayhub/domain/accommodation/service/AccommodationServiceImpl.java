package com.roastedbear.stayhub.domain.accommodation.service;

import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationCreateRequest;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationResponse;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationSummaryResponse;
import com.roastedbear.stayhub.domain.accommodation.dto.AccommodationUpdateRequest;
import com.roastedbear.stayhub.domain.accommodation.entity.Accommodation;
import com.roastedbear.stayhub.domain.accommodation.entity.AccommodationStatus;
import com.roastedbear.stayhub.domain.accommodation.repository.AccommodationRepository;
import com.roastedbear.stayhub.domain.member.entity.Member;
import com.roastedbear.stayhub.domain.member.repository.MemberRepository;
import com.roastedbear.stayhub.domain.room.dto.RoomCreateRequest;
import com.roastedbear.stayhub.domain.room.dto.RoomResponse;
import com.roastedbear.stayhub.domain.room.dto.RoomUpdateRequest;
import com.roastedbear.stayhub.domain.room.entity.Amenity;
import com.roastedbear.stayhub.domain.room.entity.Room;
import com.roastedbear.stayhub.domain.room.repository.AmenityRepository;
import com.roastedbear.stayhub.domain.room.repository.RoomRepository;
import com.roastedbear.stayhub.global.exception.BusinessException;
import com.roastedbear.stayhub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 숙소 관리 서비스 구현체
 *
 * 호스트 역할 정책:
 * - 최초 숙소 등록 시 HOST 역할 자동 부여 (Airbnb 방식)
 * - 이후 숙소/객실 CRUD 시 host.id 일치 여부만 확인
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;

    /**
     * 숙소 등록
     * - 최초 등록 시 HOST 역할 자동 부여
     */
    @Override
    @Transactional
    public AccommodationResponse createAccommodation(Long hostId, AccommodationCreateRequest request) {
        Member host = findMember(hostId);

        // 최초 숙소 등록 시 HOST 역할 부여 (이미 있으면 무시)
        if (!host.isHost()) {
            host.addHostRole();
        }

        Accommodation accommodation = Accommodation.builder()
                .host(host)
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .sido(request.getSido())
                .sigungu(request.getSigungu())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        return AccommodationResponse.from(accommodationRepository.save(accommodation));
    }

    /**
     * 숙소 상세 조회 (공개)
     * - DELETED 상태는 404 처리
     */
    @Override
    public AccommodationResponse getAccommodation(Long accommodationId) {
        Accommodation accommodation = findNonDeletedAccommodation(accommodationId);
        return AccommodationResponse.from(accommodation);
    }

    /**
     * 내 숙소 목록 조회 (호스트)
     * - ACTIVE 상태만 반환
     */
    @Override
    public Page<AccommodationSummaryResponse> getMyAccommodations(Long hostId, Pageable pageable) {
        return accommodationRepository
                .findByHostIdAndStatus(hostId, AccommodationStatus.ACTIVE, pageable)
                .map(AccommodationSummaryResponse::from);
    }

    /**
     * 숙소 수정 (호스트 본인만)
     */
    @Override
    @Transactional
    public AccommodationResponse updateAccommodation(Long hostId, Long accommodationId,
                                                     AccommodationUpdateRequest request) {
        Accommodation accommodation = findAndVerifyAccommodationHost(hostId, accommodationId);

        accommodation.update(
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        return AccommodationResponse.from(accommodation);
    }

    /**
     * 숙소 삭제 - 소프트 삭제 (호스트 본인만)
     */
    @Override
    @Transactional
    public void deleteAccommodation(Long hostId, Long accommodationId) {
        Accommodation accommodation = findAndVerifyAccommodationHost(hostId, accommodationId);
        accommodation.delete();
    }

    /**
     * 객실 등록 (해당 숙소의 호스트만)
     */
    @Override
    @Transactional
    public RoomResponse createRoom(Long hostId, Long accommodationId, RoomCreateRequest request) {
        Accommodation accommodation = findAndVerifyAccommodationHost(hostId, accommodationId);

        Room room = Room.builder()
                .accommodation(accommodation)
                .name(request.getName())
                .description(request.getDescription())
                .maxOccupancy(request.getMaxOccupancy())
                .basePrice(request.getBasePrice())
                .build();

        // 편의시설 연결 (ID 목록이 비어있지 않을 때만)
        if (!request.getAmenityIds().isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(request.getAmenityIds());
            room.updateAmenities(amenities);
        }

        return RoomResponse.from(roomRepository.save(room));
    }

    /**
     * 객실 수정 (해당 숙소의 호스트만)
     * - amenityIds로 편의시설 전체 교체
     */
    @Override
    @Transactional
    public RoomResponse updateRoom(Long hostId, Long roomId, RoomUpdateRequest request) {
        Room room = findAndVerifyRoomHost(hostId, roomId);

        room.update(
                request.getName(),
                request.getDescription(),
                request.getMaxOccupancy(),
                request.getBasePrice()
        );

        List<Amenity> amenities = amenityRepository.findAllById(request.getAmenityIds());
        room.updateAmenities(amenities);

        return RoomResponse.from(room);
    }

    /**
     * 객실 삭제 - 비활성화 (해당 숙소의 호스트만)
     * - 실제 삭제 대신 UNAVAILABLE 상태로 변경
     */
    @Override
    @Transactional
    public void deleteRoom(Long hostId, Long roomId) {
        Room room = findAndVerifyRoomHost(hostId, roomId);
        room.deactivate();
    }

    /**
     * 숙소의 객실 목록 조회 (공개)
     */
    @Override
    public Page<RoomResponse> getRooms(Long accommodationId, Pageable pageable) {
        findNonDeletedAccommodation(accommodationId);
        return roomRepository.findByAccommodationId(accommodationId, pageable)
                .map(RoomResponse::from);
    }

    // === 내부 헬퍼 메서드 ===

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Accommodation findNonDeletedAccommodation(Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOMMODATION_NOT_FOUND));

        if (accommodation.getStatus() == AccommodationStatus.DELETED) {
            throw new BusinessException(ErrorCode.ACCOMMODATION_NOT_FOUND);
        }

        return accommodation;
    }

    private Accommodation findAndVerifyAccommodationHost(Long hostId, Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOMMODATION_NOT_FOUND));

        if (!accommodation.getHost().getId().equals(hostId)) {
            throw new BusinessException(ErrorCode.NOT_ACCOMMODATION_HOST);
        }

        return accommodation;
    }

    private Room findAndVerifyRoomHost(Long hostId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getAccommodation().getHost().getId().equals(hostId)) {
            throw new BusinessException(ErrorCode.NOT_ACCOMMODATION_HOST);
        }

        return room;
    }
}
