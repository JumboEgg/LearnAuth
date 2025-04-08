package ssafy.d210.backend.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.Report;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // UserLecture entity 내의 userid로 신고 목록 조회
    List<Report> findByUserLectureUserId(Long userId);

    Optional<Report> findByUserLectureId(Long userLectureId);
}
