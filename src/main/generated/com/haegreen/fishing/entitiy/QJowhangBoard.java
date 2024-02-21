package com.haegreen.fishing.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJowhangBoard is a Querydsl query type for JowhangBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJowhangBoard extends EntityPathBase<JowhangBoard> {

    private static final long serialVersionUID = -1111921448L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJowhangBoard jowhangBoard = new QJowhangBoard("jowhangBoard");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Long> jbno = createNumber("jbno", Long.class);

    public final QMember member;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modTime = _super.modTime;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regTime = _super.regTime;

    public final NumberPath<Integer> replyCount = createNumber("replyCount", Integer.class);

    public final StringPath title = createString("title");

    public QJowhangBoard(String variable) {
        this(JowhangBoard.class, forVariable(variable), INITS);
    }

    public QJowhangBoard(Path<? extends JowhangBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJowhangBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJowhangBoard(PathMetadata metadata, PathInits inits) {
        this(JowhangBoard.class, metadata, inits);
    }

    public QJowhangBoard(Class<? extends JowhangBoard> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

