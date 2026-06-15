package com.roastedbear.stayhub.domain.accommodation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAccommodationImage is a Querydsl query type for AccommodationImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccommodationImage extends EntityPathBase<AccommodationImage> {

    private static final long serialVersionUID = -630787933L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAccommodationImage accommodationImage = new QAccommodationImage("accommodationImage");

    public final QAccommodation accommodation;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isThumbnail = createBoolean("isThumbnail");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QAccommodationImage(String variable) {
        this(AccommodationImage.class, forVariable(variable), INITS);
    }

    public QAccommodationImage(Path<? extends AccommodationImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAccommodationImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAccommodationImage(PathMetadata metadata, PathInits inits) {
        this(AccommodationImage.class, metadata, inits);
    }

    public QAccommodationImage(Class<? extends AccommodationImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.accommodation = inits.isInitialized("accommodation") ? new QAccommodation(forProperty("accommodation"), inits.get("accommodation")) : null;
    }

}

