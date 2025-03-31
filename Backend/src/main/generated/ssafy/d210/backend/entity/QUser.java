package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1137149013L;

    public static final QUser user = new QUser("user");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final ListPath<PaymentRatio, QPaymentRatio> paymentRatioList = this.<PaymentRatio, QPaymentRatio>createList("paymentRatioList", PaymentRatio.class, QPaymentRatio.class, PathInits.DIRECT2);

    public final StringPath userKey = createString("userKey");

    public final ListPath<UserLecture, QUserLecture> userLectureList = this.<UserLecture, QUserLecture>createList("userLectureList", UserLecture.class, QUserLecture.class, PathInits.DIRECT2);

    public final StringPath wallet = createString("wallet");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

