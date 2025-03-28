package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.entity.UserLecture;

import java.util.List;

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
                   ul.certificate
            from lecture l
            join user_lecture ul
            on l.lecture_id = ul.lecture_lecture_id
            join category c
            on l.category_category_id = c.category_id
            where ul.user_user_id = :userId;
         """, nativeQuery = true)
    List<CertificateResponse> getFinishedUserLecture(@Param("userId") Long userId);

    // 사용자가 보유한 강의의 이수증
    // TODO : qrCode 추가. 지금 DB에 없습니다
    @Query(value = """
            select l.title as title,
                   u.name as teacherName,
                   u.wallet as teacherWallet,
                   ul.certificate_date as certificateDate,
                   ul.certificate as certificate
            from lecture l
            join user_lecture ul
            on l.lecture_id = ul.lecture_lecture_id
            and l.lecture_id = :lectureId
            and ul.user_user_id = :userId
            join user u
            on u.user_id = ul.user_user_id
            join payment_ratio p
            on p.lecture_lecture_id = l.lecture_id
            and p.lecturer = true
         """, nativeQuery = true)
    CertificateDetailResponse getCertificateDetail(@Param("userId") Long userId, @Param("lectureId") Long lectureId);

}
