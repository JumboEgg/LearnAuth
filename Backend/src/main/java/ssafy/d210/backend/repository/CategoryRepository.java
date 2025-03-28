package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
