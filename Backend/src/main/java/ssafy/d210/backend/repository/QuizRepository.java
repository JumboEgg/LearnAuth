package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
