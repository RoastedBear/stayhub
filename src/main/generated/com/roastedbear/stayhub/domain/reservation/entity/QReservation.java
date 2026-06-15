package com.roastedbear.stayhub.domain.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = -1183843304L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final com.roastedbear.stayhub.global.entity.QBaseEntity _super = new com.roastedbear.stayhub.global.entity.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> checkInDate = createDate("checkInDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> checkOutDate = createDate("checkOutDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.roastedbear.stayhub.domain.member.entity.QMember guest;

    public final NumberPath<Integer> guestCount = createNumber("guestCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.roastedbear.stayhub.domain.room.entity.QRoom room;

    public final EnumPath<ReservationStatus> status = createEnum("status", ReservationStatus.class);

    public final NumberPath<java.math.BigDecimal> totalPrice = createNumber("totalPrice", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.guest = inits.isInitialized("guest") ? new com.roastedbear.stayhub.domain.member.entity.QMember(forProperty("guest")) : null;
        this.room = inits.isInitialized("room") ? new com.roastedbear.stayhub.domain.room.entity.QRoom(forProperty("room"), inits.get("room")) : null;
    }

}

