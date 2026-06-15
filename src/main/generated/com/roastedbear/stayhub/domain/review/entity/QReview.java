package com.roastedbear.stayhub.domain.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = -297714014L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReview review = new QReview("review");

    public final com.roastedbear.stayhub.global.entity.QBaseEntity _super = new com.roastedbear.stayhub.global.entity.QBaseEntity(this);

    public final com.roastedbear.stayhub.domain.accommodation.entity.QAccommodation accommodation;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.roastedbear.stayhub.domain.member.entity.QMember guest;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final com.roastedbear.stayhub.domain.reservation.entity.QReservation reservation;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReview(String variable) {
        this(Review.class, forVariable(variable), INITS);
    }

    public QReview(Path<? extends Review> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReview(PathMetadata metadata, PathInits inits) {
        this(Review.class, metadata, inits);
    }

    public QReview(Class<? extends Review> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.accommodation = inits.isInitialized("accommodation") ? new com.roastedbear.stayhub.domain.accommodation.entity.QAccommodation(forProperty("accommodation"), inits.get("accommodation")) : null;
        this.guest = inits.isInitialized("guest") ? new com.roastedbear.stayhub.domain.member.entity.QMember(forProperty("guest")) : null;
        this.reservation = inits.isInitialized("reservation") ? new com.roastedbear.stayhub.domain.reservation.entity.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
    }

}

