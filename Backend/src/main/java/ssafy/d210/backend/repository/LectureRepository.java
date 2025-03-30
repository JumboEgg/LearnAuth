package ssafy.d210.backend.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.dto.response.lecture.*;
import ssafy.d210.backend.entity.Lecture;
import ssafy.d210.backend.entity.SubLecture;
import ssafy.d210.backend.entity.UserLecture;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    // 카테고리별 강의 목록 조회
    @Query(value = """
            select l.lecture_id as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.sub_lecture_url as lectureUrl,
                   c.category_name as categoryName
             from lecture l
             join category c
             on l.category_category_id = c.category_id
             and (c.category_name = :category or :category is null)
             left join payment_ratio p
             on l.lecture_id = p.lecture_lecture_id
             and p.lecturer = true
             left join user u
             on p.user_user_id = u.user_id
             left join (
                select sl1.lecture_lecture_id, sl1.sub_lecture_url
                from sub_lecture sl1
                join (
                    select
                        lecture_lecture_id,
                        MIN(sub_lecture_id) as sub_lecture_id
                    from sub_lecture sl2
                    group by lecture_lecture_id
                    order by MIN(sub_lecture_id) asc
                ) sl2
                on sl1.sub_lecture_id = sl2.sub_lecture_id
             ) sl
             on l.lecture_id = sl.lecture_lecture_id
             order by l.lecture_id
             limit 12
             offset :offset;
        """, nativeQuery = true)
    List<LectureInfoResponse> getLecturesByCategory(String category, int offset);

    // 메인 화면 강의 목록
    // 최대 완료 수 강의
    @Query(value = """
            select l.lecture_id as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.sub_lecture_url as lectureUrl,
                   c.category_name as categoryName
             from lecture l
             join (
                select lecture_lecture_id, COUNT(*)
                from user_lecture
                where certificate_date is not null
                group by lecture_lecture_id
                order by COUNT(*) desc
                limit 3
             ) ul
             on l.lecture_id = ul.lecture_lecture_id
             join category c
             on l.category_category_id = c.category_id
             left join payment_ratio p
             on l.lecture_id = p.lecture_lecture_id
             and p.lecturer = true
             left join user u
             on p.user_user_id = u.user_id
             left join (
                select sl1.lecture_lecture_id,
                    sl1.sub_lecture_url
                from sub_lecture sl1
                join (
                    select lecture_lecture_id,
                        MIN(sub_lecture_id) as min_sub_lecture_id
                    from sub_lecture
                    group by lecture_lecture_id
                ) sl2
                on sl1.sub_lecture_id = sl2.min_sub_lecture_id
             ) sl
             on l.lecture_id = sl.lecture_lecture_id;
        """, nativeQuery = true)
    List<LectureInfoResponse> getMostFinishedLectures();

    // 무작위 강의
    @Query(value = """
            select
                l.lecture_id as lectureId,
                l.title as title,
                l.price as price,
                u.name as lecturer,
                sl.sub_lecture_url as lectureUrl,
                c.category_name as categoryName
            from lecture l
            join lateral (
                select lecture_id
                from lecture 
                order by RAND() 
                limit 10
            ) r
            on l.lecture_id = r.lecture_id
            join category c
            on l.category_category_id = c.category_id
            left join payment_ratio p 
            on l.lecture_id = p.lecture_lecture_id
            and p.lecturer = true
            left join user u
            on p.user_user_id = u.user_id
            left join (
                select 
                    sl1.lecture_lecture_id,
                    sl1.sub_lecture_url
                from sub_lecture sl1
                join (
                    SELECT lecture_lecture_id,
                        MIN(sub_lecture_id) as min_sub_lecture_id
                    FROM sub_lecture
                    GROUP BY lecture_lecture_id
                ) sl2
                on sl1.sub_lecture_id = sl2.min_sub_lecture_id
            ) sl ON l.lecture_id = sl.lecture_lecture_id
        """,
        nativeQuery = true)
    List<LectureInfoResponse> getRandomLectures();

    // 최신 강의
    @Query(value = """
            select l.lecture_id as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.sub_lecture_url as lectureUrl,
                   c.category_name as categoryName
            from lecture l
            join category c
            on l.category_category_id = c.category_id
            left join payment_ratio p
            on l.lecture_id = p.lecture_lecture_id
            and p.lecturer = true
            left join user u
            on p.user_user_id = u.user_id
            left join (
                select sl1.lecture_lecture_id, sl1.sub_lecture_url
                from sub_lecture sl1
                join (
                    select lecture_lecture_id,
                        MIN(sub_lecture_id) as min_sub_lecture_id
                    from sub_lecture
                    group by lecture_lecture_id
                ) sl2
                on sl1.sub_lecture_id = sl2.min_sub_lecture_id
            ) sl
            on l.lecture_id = sl.lecture_lecture_id
            order by l.lecture_id desc
            limit 10
        """, nativeQuery = true)
    List<LectureInfoResponse> getNewestLectures();


    // 강의 상세 조회
    // 강의 정보
    @Query(value = """
            select l.lecture_id as lectureId,
                   l.title as title,
                   l.price as price,
                   l.goal as goal,
                   l.description as description,
                   u.name as lecturer,
                   sl.sub_lecture_url as lectureUrl,
                   c.category_name as categoryName
             from lecture l
             join category c
             on l.category_category_id = c.category_id
             and l.lecture_id = :lectureId
             left join payment_ratio p
             on l.lecture_id = p.lecture_lecture_id
             and p.lecturer = true
             left join user u
             on p.user_user_id = u.user_id
             left join (
                select sl1.lecture_lecture_id,
                    sl1.sub_lecture_url
                from sub_lecture sl1
                join (
                    select lecture_lecture_id,
                        MIN(sub_lecture_id) as sub_lecture_id
                    from sub_lecture
                    group by lecture_lecture_id
                ) sl2
                on sl1.sub_lecture_id = sl2.sub_lecture_id
             ) sl
             on l.lecture_id = sl.lecture_lecture_id
        """, nativeQuery = true)
    LectureDetail getLectureById(Long lectureId);

    // 세부 강의 정보
    @Query(value = """
        select sub_lecture_id
        from sub_lecture
        where lecture_lecture_id = :lectureId
        order by sub_lecture_id;
    """, nativeQuery = true)
    List<Integer> getSublecturesById(Long lectureId);

    // 세부 강의 id로 사용자 강의 조회 정보 조회
    @Query(value = """
        select
            sl.sub_lecture_id as subLectureId,
            sl.sub_lecture_title as subLectureTitle,
            sl.sub_lecture_url as lectureUrl,
            sl.sub_lecture_length as lectureLength,
            ult.continue_watching as continueWatching
        from sub_lecture sl
        join user_lecture_time as ult
        on sl.sub_lecture_id = ult.sub_lecture_sub_lecture_id
        and sl.sub_lecture_id in (:subLectureIdList)
        order by sl.sub_lecture_id;
    """, nativeQuery = true)
    List<SubLectureDetailResponse> getUserLectureTime(List<Integer> subLectureIdList);

    // 강의 검색
    // 검색어 기반 강의 리스트 반환
    @Query("""
        SELECT new ssafy.d210.backend.dto.response.lecture.LectureInfoResponse(
            l.id, l.title, l.price, u.name, l.walletKey, c.categoryName)
        FROM Lecture l
        JOIN l.category c
        JOIN PaymentRatio pr ON pr.lecture = l AND pr.lecturer = 1
        JOIN pr.user u
        WHERE l.title LIKE CONCAT('%', :keyword, '%')
        ORDER BY l.id DESC
    """)
    List<LectureInfoResponse> searchLecturesByKeyword(
            @Param("keyword") String keyword
            // offset, limit은 EntityManager로 처리
    );

    // 검색어 기반 전체 개수 반환
    @Query("""
        SELECT COUNT(1)
        FROM Lecture l
        WHERE l.title LIKE CONCAT('%', :keyword, '%')
    """)
    int countLecturesByKeyword(@Param("keyword") String keyword);

    // 강의 구매


    // 사용자가 보유한 강의
    @Query(value = """
            select l.lecture_id as lectureId,
                   c.category_name as categoryName,
                   l.title as title,
                   u.name as lecturer,
                   u.lecturer as isLecturer,
                   ul.recent_lecture_id as recentId
            from lecture l
            join (
                select
                    uLect.lecture_lecture_id as lecture_id,
                    uLect.recent_lecture_id as recent_lecture_id,
                    (
                        select (COUNT(CASE WHEN end_flag = TRUE THEN 1 END) * 1.0) / COUNT(*)
                        from user_lecture_time
                        where user_lecture_id = uLect.user_lecture_id
                    ) as learning_rate
                from user_lecture uLect
                where uLect.user_user_id = :userId
            ) ul
            on l.lecture_id = ul.lecture_id
            join category c
            on l.category_category_id = c.category_id
            left join (
                select
                    p.lecture_lecture_id as lecture_id,
                    u.name as name,
                    p.lecturer
                from user u
                join payment_ratio p
                on u.user_id = p.user_user_id
                where p.lecturer = true
            ) u
            on u.lecture_id = l.lecture_id;
         """, nativeQuery = true)
    List<LectureResponse> getPurchasedLectures(@Param("userId") Long userId);

    // 사용자가 참여한 강의
    @Query(value = """
            select l.lecture_id as lectureId,
                   c.category_name as categoryName,
                   l.title as title,
                   u.name as lecturer,
                   u.lecturer as isLecturer,
                   0 as recentId
            from lecture l
            left join (
                select
                    p.lecture_lecture_id as lecture_id,
                    u.name as name,
                    p.lecturer
                from user u
                join payment_ratio p
                on u.user_id = p.user_user_id
                and p.user_user_id = :userId
            ) u
            on u.lecture_id = l.lecture_id
            join category c
            on l.category_category_id = c.category_id
         """, nativeQuery = true)
    List<LectureResponse> getParticipatedLectures(@Param("userId") Long userId);
}
