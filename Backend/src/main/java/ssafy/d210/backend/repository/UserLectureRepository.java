package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.UserLecture;

import java.util.List;

public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {
    List<UserLecture> findAllByUserId(Long id);
}
