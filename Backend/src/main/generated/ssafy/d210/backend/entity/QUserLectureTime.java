package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserLectureTime is a Querydsl query type for UserLectureTime
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLectureTime extends EntityPathBase<UserLectureTime> {

    private static final long serialVersionUID = -2081394218L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserLectureTime userLectureTime = new QUserLectureTime("userLectureTime");

    public final StringPath continueWatching = createString("continueWatching");

    public final NumberPath<Integer> endFlag = createNumber("endFlag", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSubLecture subLecture;

    public final QUserLecture userLecture;

    public QUserLectureTime(String variable) {
        this(UserLectureTime.class, forVariable(variable), INITS);
    }

    public QUserLectureTime(Path<? extends UserLectureTime> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserLectureTime(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserLectureTime(PathMetadata metadata, PathInits inits) {
        this(UserLectureTime.class, metadata, inits);
    }

    public QUserLectureTime(Class<? extends UserLectureTime> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.subLecture = inits.isInitialized("subLecture") ? new QSubLecture(forProperty("subLecture"), inits.get("subLecture")) : null;
        this.userLecture = inits.isInitialized("userLecture") ? new QUserLecture(forProperty("userLecture"), inits.get("userLecture")) : null;
    }

}

