package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.entity.Quiz;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // 첫 번째 쿼리에서 랜덤 퀴즈 ID 3개를 가져옴
    @Query("SELECT q FROM Quiz q WHERE q.lecture.id = :lectureId")
    List<Quiz> findAllByLectureId(@Param("lectureId") Long lectureId);


    // 두 번째 쿼리에서 해당 ID의 퀴즈와 옵션을 함께 가져옴
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.quizOptionlist WHERE q.id IN :quizIds")
    List<Quiz> findQuizzesWithOptionsByIds(@Param("quizIds") List<Long> quizIds);
}
