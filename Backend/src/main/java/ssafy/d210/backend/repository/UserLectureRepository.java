package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.entity.UserLecture;

import java.util.List;

public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {

    public List<UserLecture> findAllByUserId(Long userId);
    // 사용자가 참여한 강의
    @Query(value = """
            select l.lectureId as lectureId,
                   c.categoryName as categoryName,
                   l.title as title,
                   l.goal as goal,
                   0 as learningRate,
                   u.lecturer as teacher,
                   0 as recentId
            from lecture l
            join (
                select p.lectureId as lectureId, u.name as lecturer
                from user u
                join paymentRatio p
                on user.userId = p.userId
                and p.userId = :userId
            ) u
            on u.lectureId = l.lectureId
            join category c
            on l.categoryId = c.categoryId;
         """, nativeQuery = true)
    List<LectureResponse> getParticipatedLectures(@Param("userId") Long userId);

    // 사용자가 보유한 강의
    @Query(value = """
            select l.lectureId as lectureId,
                   c.categoryName as categoryName,
                   l.title as title,
                   l.goal as goal,
                   ul.learningRate as learningRate,
                   u.lecturer as teacher,
                   ul.recentLectId as recentId
            from lecture l
            join (
                select
                    uLect.lectureId as lectureId,
                    uLect.recentLectId as recentLectId,
                    (
                        select (COUNT(CASE WHEN endflag = TRUE THEN 1 END) * 1.0) / COUNT(*)
                        from userLectureTime
                        where userLectureId = uLect.userLectureId
                    ) as learningRate
                from userlecture uLect
                where uLect.userId = :userId
            ) ul
            on l.lectureId = ul.lectureId
            join category c
            on l.categoryId = c.categoryId
            join (
                select p.lectureId as lectureId, u.name as lecturer
                from user u
                join paymentRatio p
                where p.lecturer = true
                on user.userId = p.userId
            ) u
            on u.lectureId = l.lectureId;
         """, nativeQuery = true)
    List<LectureResponse> getUserLectures(@Param("userId") Long userId);

    // 사용자가 이수를 완료한 강의
    @Query(value = """
            select l.lectureId as lectureId,
                   l.title as title,
                   c.categoryName as categoryName,
                   ul.certificate,
            from lecture l
            join userLecture ul
            on l.lectureId = ul.lectureId
            join category c
            on l.categoryId = c.categoryId
            where ul.userId = :userId;
         """, nativeQuery = true)
    List<CertificateResponse> getFinishedUserLecture(@Param("userId") Long userId);

    // 사용자가 보유한 강의의 이수증
    @Query(value = """
            select l.title as title,
                   u.name as teacherName,
                   u.wallet as teacherWallet,
                   ul.certificateDate as certificateDate,
                   ul.certificate as certificate,
                   ul.qrCode as qrCode
            from lecture l
            join userLecture ul
            on l.lectureId = ul.lectureId
            and l.lectureId = :lectureId
            and ul.userId = :userId
            join user u
            on u.userId = ul.userId
            join paymentRatio p
            on p.lectureId = l.lectureId
            and p.lecturer = TRUE
         """, nativeQuery = true)
    CertificateDetailResponse getCertificateDetail(@Param("userId") Long userId, @Param("lectureId") Long lectureId);

}
