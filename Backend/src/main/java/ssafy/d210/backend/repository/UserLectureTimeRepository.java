package ssafy.d210.backend.repository;
//
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.entity.UserLectureTime;

public interface UserLectureTimeRepository extends JpaRepository<UserLectureTime, Long> {

    UserLectureTime findByUserLectureIdAndSubLectureId(Long userLectureId, Long subLectureId);

    @Query(value = """
        select COUNT(*)
        from UserLectureTime ult
        join UserLecture ul
        on ult.userLecture.id = ul.id
        and ult.endFlag = 1
        and ul.id = :lectureId
    """)
    int countUserLectureTimesByLectureId(@Param("lectureId") Long lectureId);
}
