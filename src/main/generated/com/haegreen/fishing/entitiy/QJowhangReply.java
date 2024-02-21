package com.haegreen.fishing.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJowhangReply is a Querydsl query type for JowhangReply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJowhangReply extends EntityPathBase<JowhangReply> {

    private static final long serialVersionUID = -1097428772L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJowhangReply jowhangReply = new QJowhangReply("jowhangReply");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final QJowhangBoard jowhangBoard;

    public final NumberPath<Long> jrno = createNumber("jrno", Long.class);

    public final QMember member;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modTime = _super.modTime;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regTime = _super.regTime;

    public final StringPath text = createString("text");

    public QJowhangReply(String variable) {
        this(JowhangReply.class, forVariable(variable), INITS);
    }

    public QJowhangReply(Path<? extends JowhangReply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJowhangReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJowhangReply(PathMetadata metadata, PathInits inits) {
        this(JowhangReply.class, metadata, inits);
    }

    public QJowhangReply(Class<? extends JowhangReply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.jowhangBoard = inits.isInitialized("jowhangBoard") ? new QJowhangBoard(forProperty("jowhangBoard"), inits.get("jowhangBoard")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

