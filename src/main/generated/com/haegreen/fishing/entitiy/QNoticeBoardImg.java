package com.haegreen.fishing.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNoticeBoardImg is a Querydsl query type for NoticeBoardImg
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNoticeBoardImg extends EntityPathBase<NoticeBoardImg> {

    private static final long serialVersionUID = 511011659L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNoticeBoardImg noticeBoardImg = new QNoticeBoardImg("noticeBoardImg");

    public final NumberPath<Long> nino = createNumber("nino", Long.class);

    public final QNoticeBoard noticeBoard;

    public final StringPath realfileName = createString("realfileName");

    public final StringPath uuidfileName = createString("uuidfileName");

    public QNoticeBoardImg(String variable) {
        this(NoticeBoardImg.class, forVariable(variable), INITS);
    }

    public QNoticeBoardImg(Path<? extends NoticeBoardImg> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNoticeBoardImg(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNoticeBoardImg(PathMetadata metadata, PathInits inits) {
        this(NoticeBoardImg.class, metadata, inits);
    }

    public QNoticeBoardImg(Class<? extends NoticeBoardImg> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.noticeBoard = inits.isInitialized("noticeBoard") ? new QNoticeBoard(forProperty("noticeBoard"), inits.get("noticeBoard")) : null;
    }

}

