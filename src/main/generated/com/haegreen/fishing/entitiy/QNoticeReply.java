package com.haegreen.fishing.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNoticeReply is a Querydsl query type for NoticeReply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoticeReply extends EntityPathBase<NoticeReply> {

    private static final long serialVersionUID = -1746093732L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNoticeReply noticeReply = new QNoticeReply("noticeReply");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final QMember member;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modTime = _super.modTime;

    public final QNoticeBoard noticeBoard;

    public final NumberPath<Long> nrno = createNumber("nrno", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regTime = _super.regTime;

    public final StringPath text = createString("text");

    public QNoticeReply(String variable) {
        this(NoticeReply.class, forVariable(variable), INITS);
    }

    public QNoticeReply(Path<? extends NoticeReply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNoticeReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNoticeReply(PathMetadata metadata, PathInits inits) {
        this(NoticeReply.class, metadata, inits);
    }

    public QNoticeReply(Class<? extends NoticeReply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
        this.noticeBoard = inits.isInitialized("noticeBoard") ? new QNoticeBoard(forProperty("noticeBoard"), inits.get("noticeBoard")) : null;
    }

}

