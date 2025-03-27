package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.QuizOption;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
}
