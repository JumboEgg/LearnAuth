package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuiz is a Querydsl query type for Quiz
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuiz extends EntityPathBase<Quiz> {

    private static final long serialVersionUID = 1137031903L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuiz quiz = new QQuiz("quiz");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLecture lecture;

    public final StringPath question = createString("question");

    public final ListPath<QuizOption, QQuizOption> quizOptionList = this.<QuizOption, QQuizOption>createList("quizOptionList", QuizOption.class, QQuizOption.class, PathInits.DIRECT2);

    public QQuiz(String variable) {
        this(Quiz.class, forVariable(variable), INITS);
    }

    public QQuiz(Path<? extends Quiz> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuiz(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuiz(PathMetadata metadata, PathInits inits) {
        this(Quiz.class, metadata, inits);
    }

    public QQuiz(Class<? extends Quiz> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
    }

}

