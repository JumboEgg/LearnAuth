package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubLecture is a Querydsl query type for SubLecture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubLecture extends EntityPathBase<SubLecture> {

    private static final long serialVersionUID = 967764008L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubLecture subLecture = new QSubLecture("subLecture");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLecture lecture;

    public final NumberPath<Integer> subLectureLength = createNumber("subLectureLength", Integer.class);

    public final StringPath subLectureTitle = createString("subLectureTitle");

    public final StringPath subLectureUrl = createString("subLectureUrl");

    public final ListPath<UserLectureTime, QUserLectureTime> userLectureTimeList = this.<UserLectureTime, QUserLectureTime>createList("userLectureTimeList", UserLectureTime.class, QUserLectureTime.class, PathInits.DIRECT2);

    public QSubLecture(String variable) {
        this(SubLecture.class, forVariable(variable), INITS);
    }

    public QSubLecture(Path<? extends SubLecture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubLecture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubLecture(PathMetadata metadata, PathInits inits) {
        this(SubLecture.class, metadata, inits);
    }

    public QSubLecture(Class<? extends SubLecture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
    }

}

