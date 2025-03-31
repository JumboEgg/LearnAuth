package ssafy.d210.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCategory is a Querydsl query type for Category
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategory extends EntityPathBase<Category> {

    private static final long serialVersionUID = 1679818888L;

    public static final QCategory category = new QCategory("category");

    public final EnumPath<ssafy.d210.backend.enumeration.CategoryName> categoryName = createEnum("categoryName", ssafy.d210.backend.enumeration.CategoryName.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<Lecture, QLecture> lectureList = this.<Lecture, QLecture>createList("lectureList", Lecture.class, QLecture.class, PathInits.DIRECT2);

    public QCategory(String variable) {
        super(Category.class, forVariable(variable));
    }

    public QCategory(Path<? extends Category> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategory(PathMetadata metadata) {
        super(Category.class, metadata);
    }

}

