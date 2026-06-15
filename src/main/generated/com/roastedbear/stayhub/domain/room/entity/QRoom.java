package com.roastedbear.stayhub.domain.room.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoom is a Querydsl query type for Room
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoom extends EntityPathBase<Room> {

    private static final long serialVersionUID = -676892440L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoom room = new QRoom("room");

    public final com.roastedbear.stayhub.global.entity.QBaseEntity _super = new com.roastedbear.stayhub.global.entity.QBaseEntity(this);

    public final com.roastedbear.stayhub.domain.accommodation.entity.QAccommodation accommodation;

    public final ListPath<Amenity, QAmenity> amenities = this.<Amenity, QAmenity>createList("amenities", Amenity.class, QAmenity.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> basePrice = createNumber("basePrice", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxOccupancy = createNumber("maxOccupancy", Integer.class);

    public final StringPath name = createString("name");

    public final EnumPath<RoomStatus> status = createEnum("status", RoomStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoom(String variable) {
        this(Room.class, forVariable(variable), INITS);
    }

    public QRoom(Path<? extends Room> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoom(PathMetadata metadata, PathInits inits) {
        this(Room.class, metadata, inits);
    }

    public QRoom(Class<? extends Room> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.accommodation = inits.isInitialized("accommodation") ? new com.roastedbear.stayhub.domain.accommodation.entity.QAccommodation(forProperty("accommodation"), inits.get("accommodation")) : null;
    }

}

