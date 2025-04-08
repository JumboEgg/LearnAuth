package ssafy.d210.backend.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.entity.UserLecture;

import java.util.List;
import java.util.Optional;

public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {

    // 사용자 강의 정보 조회
    // 사용자의 강의 보유 여부, 특정 행 조회에 사용
    @Query(value = """
            select *
            from user_lecture
            where lecture_lecture_id = :lectureId
            and user_user_id = :userId;
        """, nativeQuery = true)
    UserLecture getUserLectureById(@Param("lectureId") Long lectureId, @Param("userId") Long userId);

    public List<UserLecture> findAllByUserId(Long userId);

    // 사용자가 이수를 완료한 강의
    @Query(value = """
            select l.lecture_id as lectureId,
                   l.title as title,
                   c.category_name as categoryName,
                   ul.certificate as certificate,
                   ul.certificate_date as certificateDate
            from lecture l
            join user_lecture ul
            on l.lecture_id = ul.lecture_lecture_id
            join category c
            on l.category_category_id = c.category_id
            where ul.user_user_id = :userId
            and ul.certificate_date is not null;
         """, nativeQuery = true)
    List<CertificateResponse> getFinishedUserLecture(@Param("userId") Long userId);

    // 사용자가 보유한 강의의 이수증
    @Query(value = """
            select l.title as title,
                   u.name as teacherName,
                   u.wallet as teacherWallet,
                   ul.certificate_date as certificateDate,
                   ul.certificate as certificate,
                   ul.qr_code as qrCode
            from lecture l
            join user_lecture ul
            on l.lecture_id = ul.lecture_lecture_id
            and l.lecture_id = :lectureId
            and ul.user_user_id = :userId
            join payment_ratio p
            on p.lecture_lecture_id = l.lecture_id
            and p.lecturer = true
            join user u
            on u.user_id = p.user_user_id
         """, nativeQuery = true)
    CertificateDetailResponse getCertificateDetail(@Param("userId") Long userId, @Param("lectureId") Long lectureId);

    // Report create 부분에 사용하는 코드
    Optional<UserLecture> findByUserIdAndLectureId(Long userId, Long lectureId);

    // 특정 강의를 수강하는 수강생 수
    @Query(value = """
        select COUNT(*)
        from user_lecture
        where lecture_lecture_id = :lectureId;
    """, nativeQuery = true)
    int countUserLectureByLectureId(@Param("lectureId") Long lectureId);
}

