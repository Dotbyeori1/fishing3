package com.haegreen.fishing.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservationDate is a Querydsl query type for ReservationDate
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservationDate extends EntityPathBase<ReservationDate> {

    private static final long serialVersionUID = -934919420L;

    public static final QReservationDate reservationDate = new QReservationDate("reservationDate");

    public final BooleanPath available = createBoolean("available");

    public final BooleanPath dateModify = createBoolean("dateModify");

    public final NumberPath<Integer> extrasMembers = createNumber("extrasMembers", Integer.class);

    public final NumberPath<Integer> fishingMoney = createNumber("fishingMoney", Integer.class);

    public final StringPath fishingSort = createString("fishingSort");

    public final StringPath message = createString("message");

    public final NumberPath<Long> rdate = createNumber("rdate", Long.class);

    public final DatePath<java.time.LocalDate> regDate = createDate("regDate", java.time.LocalDate.class);

    public final ListPath<Reservation, QReservation> reservations = this.<Reservation, QReservation>createList("reservations", Reservation.class, QReservation.class, PathInits.DIRECT2);

    public QReservationDate(String variable) {
        super(ReservationDate.class, forVariable(variable));
    }

    public QReservationDate(Path<? extends ReservationDate> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReservationDate(PathMetadata metadata) {
        super(ReservationDate.class, metadata);
    }

}

