package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLecture is a Querydsl query type for Lecture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLecture extends EntityPathBase<Lecture> {

    private static final long serialVersionUID = -1695862604L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLecture lecture = new QLecture("lecture");

    public final QCategory category;

    public final StringPath description = createString("description");

    public final StringPath goal = createString("goal");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<PaymentRatio, QPaymentRatio> paymentRatioList = this.<PaymentRatio, QPaymentRatio>createList("paymentRatioList", PaymentRatio.class, QPaymentRatio.class, PathInits.DIRECT2);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final ListPath<Quiz, QQuiz> quizList = this.<Quiz, QQuiz>createList("quizList", Quiz.class, QQuiz.class, PathInits.DIRECT2);

    public final ListPath<SubLecture, QSubLecture> subLectureList = this.<SubLecture, QSubLecture>createList("subLectureList", SubLecture.class, QSubLecture.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final ListPath<UserLecture, QUserLecture> userLectureList = this.<UserLecture, QUserLecture>createList("userLectureList", UserLecture.class, QUserLecture.class, PathInits.DIRECT2);

    public QLecture(String variable) {
        this(Lecture.class, forVariable(variable), INITS);
    }

    public QLecture(Path<? extends Lecture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLecture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLecture(PathMetadata metadata, PathInits inits) {
        this(Lecture.class, metadata, inits);
    }

    public QLecture(Class<? extends Lecture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category")) : null;
    }

}

