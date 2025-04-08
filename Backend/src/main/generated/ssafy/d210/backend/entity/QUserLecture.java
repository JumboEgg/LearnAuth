package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserLecture is a Querydsl query type for UserLecture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLecture extends EntityPathBase<UserLecture> {

    private static final long serialVersionUID = 1033668457L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserLecture userLecture = new QUserLecture("userLecture");

    public final NumberPath<Integer> certificate = createNumber("certificate", Integer.class);

    public final DatePath<java.time.LocalDate> certificateDate = createDate("certificateDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLecture lecture;

    public final StringPath qrCode = createString("qrCode");

    public final NumberPath<Long> recentLectureId = createNumber("recentLectureId", Long.class);

    public final NumberPath<Integer> report = createNumber("report", Integer.class);

    public final QUser user;

    public final ListPath<UserLectureTime, QUserLectureTime> userLectureTimeList = this.<UserLectureTime, QUserLectureTime>createList("userLectureTimeList", UserLectureTime.class, QUserLectureTime.class, PathInits.DIRECT2);

    public QUserLecture(String variable) {
        this(UserLecture.class, forVariable(variable), INITS);
    }

    public QUserLecture(Path<? extends UserLecture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserLecture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserLecture(PathMetadata metadata, PathInits inits) {
        this(UserLecture.class, metadata, inits);
    }

    public QUserLecture(Class<? extends UserLecture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

