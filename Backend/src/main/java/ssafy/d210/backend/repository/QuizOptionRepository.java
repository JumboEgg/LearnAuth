package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.entity.QuizOption;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {

    @Query("SELECT o FROM QuizOption o WHERE o.quiz.id IN :quizIds")
    List<QuizOption> findByQuizIdIn(@Param("quizIds") List<Long> quizIds);
}
