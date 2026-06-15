package com.roastedbear.stayhub.domain.accommodation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAccommodation is a Querydsl query type for Accommodation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccommodation extends EntityPathBase<Accommodation> {

    private static final long serialVersionUID = 716556184L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAccommodation accommodation = new QAccommodation("accommodation");

    public final com.roastedbear.stayhub.global.entity.QBaseEntity _super = new com.roastedbear.stayhub.global.entity.QBaseEntity(this);

    public final StringPath address = createString("address");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final com.roastedbear.stayhub.domain.member.entity.QMember host;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath name = createString("name");

    public final StringPath sido = createString("sido");

    public final StringPath sigungu = createString("sigungu");

    public final EnumPath<AccommodationStatus> status = createEnum("status", AccommodationStatus.class);

    public final EnumPath<AccommodationType> type = createEnum("type", AccommodationType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAccommodation(String variable) {
        this(Accommodation.class, forVariable(variable), INITS);
    }

    public QAccommodation(Path<? extends Accommodation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAccommodation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAccommodation(PathMetadata metadata, PathInits inits) {
        this(Accommodation.class, metadata, inits);
    }

    public QAccommodation(Class<? extends Accommodation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.host = inits.isInitialized("host") ? new com.roastedbear.stayhub.domain.member.entity.QMember(forProperty("host")) : null;
    }

}

