package ssafy.d210.backend.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.Category;
import ssafy.d210.backend.enumeration.CategoryName;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryName(CategoryName categoryName);
}
