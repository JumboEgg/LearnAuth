package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentRatio is a Querydsl query type for PaymentRatio
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentRatio extends EntityPathBase<PaymentRatio> {

    private static final long serialVersionUID = 603844655L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentRatio paymentRatio = new QPaymentRatio("paymentRatio");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLecture lecture;

    public final NumberPath<Integer> lecturer = createNumber("lecturer", Integer.class);

    public final NumberPath<Integer> ratio = createNumber("ratio", Integer.class);

    public final QUser user;

    public QPaymentRatio(String variable) {
        this(PaymentRatio.class, forVariable(variable), INITS);
    }

    public QPaymentRatio(Path<? extends PaymentRatio> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentRatio(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentRatio(PathMetadata metadata, PathInits inits) {
        this(PaymentRatio.class, metadata, inits);
    }

    public QPaymentRatio(Class<? extends PaymentRatio> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

