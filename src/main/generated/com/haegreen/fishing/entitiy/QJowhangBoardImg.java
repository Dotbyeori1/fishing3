package com.haegreen.fishing.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJowhangBoardImg is a Querydsl query type for JowhangBoardImg
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJowhangBoardImg extends EntityPathBase<JowhangBoardImg> {

    private static final long serialVersionUID = 1830970315L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJowhangBoardImg jowhangBoardImg = new QJowhangBoardImg("jowhangBoardImg");

    public final NumberPath<Long> jino = createNumber("jino", Long.class);

    public final QJowhangBoard jowhangBoard;

    public final StringPath realfileName = createString("realfileName");

    public final StringPath uuidfileName = createString("uuidfileName");

    public QJowhangBoardImg(String variable) {
        this(JowhangBoardImg.class, forVariable(variable), INITS);
    }

    public QJowhangBoardImg(Path<? extends JowhangBoardImg> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJowhangBoardImg(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJowhangBoardImg(PathMetadata metadata, PathInits inits) {
        this(JowhangBoardImg.class, metadata, inits);
    }

    public QJowhangBoardImg(Class<? extends JowhangBoardImg> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.jowhangBoard = inits.isInitialized("jowhangBoard") ? new QJowhangBoard(forProperty("jowhangBoard"), inits.get("jowhangBoard")) : null;
    }

}

