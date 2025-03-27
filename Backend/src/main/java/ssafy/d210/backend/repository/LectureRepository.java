package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.d210.backend.dto.response.lecture.LectureDetailResponse;
import ssafy.d210.backend.dto.response.lecture.LectureInfoResponse;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.dto.response.lecture.SubLectureDetailResponse;
import ssafy.d210.backend.entity.Lecture;
import ssafy.d210.backend.entity.UserLecture;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    // 카테고리별 강의 목록 조회
    @Query(value = """
            select l.lectureId as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.subLectureUrl as lectureUrl,
                   c.categoryName as categoryName
             from lecture l
             join category c
             on l.categoryId = c.categoryId
             and (c.categoryName = :category or :category is null)
             join paymentRatio p
             on l.lectureId = p.lectureId
             and p.lecturer = true
             join user u
             on p.userId = u.userId
             join (
                select sl1.lectureId, sl1.subLectureUrl
                from subLecture sl1
                join (
                    select lectureId, MIN(subLectureId)
                    from subLectureId
                    group by lectureId
                ) sl2
                on sl1.subLectureId = sl2.subLectureId
             ) sl
             on l.lectureId = sl.lectureId
             order by l.lectureId
             limit 12
             offset (:page - 1) * 12;
        """, nativeQuery = true)
    List<LectureInfoResponse> getLecturesByCategory(String category, int page);

    // 메인 화면 강의 목록
    // 최대 완료 수 강의
    @Query(value = """
            select l.lectureId as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.subLectureUrl as lectureUrl,
                   c.categoryName as categoryName
             from lecture l
             join (
                select lectureId, COUNT(*)
                from userLecture
                where certificateDate is not null
                group by lectureId
                order by COUNT(*) desc
                limit 3
             ) ul
             on l.lectureId = ul.lectureId
             join category c
             on l.categoryId = c.categoryId
             join paymentRatio p
             on l.lectureId = p.lectureId
             and p.lecturer = true
             join user u
             on p.userId = u.userId
             join (
                select sl1.lectureId, sl1.subLectureUrl
                from subLecture sl1
                join (
                    select lectureId,
                        MIN(subLectureId) as subLectureId
                    from subLectureId
                    group by lectureId
                ) sl2
                on sl1.subLectureId = sl2.subLectureId
             ) sl
             on l.lectureId = sl.lectureId;
        """, nativeQuery = true)
    List<LectureInfoResponse> getMostFinishedLectures();

    // 무작위 강의
    @Query(value = """
            select 
                l.lectureId,
                l.title,
                l.price,
                u.name as lecturer,
                sl.subLectureUrl as lectureUrl,
                c.categoryName as categoryName
            from lecture l
            join lateral (
                select lectureId 
                from lecture 
                order by RAND() 
                limit 10
            ) r on l.lectureId = r.lectureId
            join category c
            on l.categoryId = c.categoryId
            join paymentRatio p 
            on l.lectureId = p.lectureId
            and p.lecturer = true
            join user u
            on p.userId = u.userId
            join (
                select 
                    sl1.lectureId,
                    sl1.subLectureUrl
                from subLecture sl1
                join (
                    SELECT lectureId, MIN(subLectureId) AS minSubLectureId
                    FROM subLecture
                    GROUP BY lectureId
                ) sl2 ON sl1.subLectureId = sl2.minSubLectureId
            ) sl ON l.lectureId = sl.lectureId
        """,
        nativeQuery = true)
    List<LectureInfoResponse> getRandomLectures();

    // 최신 강의
    @Query(value = """
            select l.lectureId as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.subLectureUrl as lectureUrl,
                   c.categoryName as categoryName
             from lecture l
             join category c
             on l.categoryId = c.categoryId
             join paymentRatio p
             on l.lectureId = p.lectureId
             and p.lecturer = true
             join user u
             on p.userId = u.userId
             join (
                select sl1.lectureId, sl1.subLectureUrl
                from subLecture sl1
                join (
                    select lectureId, MIN(subLectureId)
                    from subLectureId
                    group by lectureId
                ) sl2
                on sl1.subLectureId = sl2.subLectureId
             ) sl
             on l.lectureId = sl.lectureId
             order by l.lectureId desc
             limit 10
        """, nativeQuery = true)
    List<LectureInfoResponse> getNewestLectures();


    // 강의 상세 조회
    // 강의 보유 여부 확인
    @Query(value = """
        select userLectureId
        from userLecture
        where lectureId = :lectureId
        and userId = :userId;
    """, nativeQuery = true)
    Boolean findUserLectureById(Long lectureId, Long userId);

    // 강의 정보 조회
    @Query(value = """
            select l.lectureId as lectureId,
                   l.title as title,
                   l.price as price,
                   u.name as lecturer,
                   sl.subLectureUrl as lectureUrl,
                   c.categoryName as categoryName
             from lecture l
             join category c
             on l.categoryId = c.categoryId
             join paymentRatio p
             on l.lectureId = p.lectureId
             and p.lecturer = true
             join user u
             on p.userId = u.userId
             join (
                select sl1.lectureId, sl1.subLectureUrl
                from subLecture sl1
                join (
                    select lectureId, MIN(subLectureId)
                    from subLectureId
                    group by lectureId
                ) sl2
                on sl1.subLectureId = sl2.subLectureId
             ) sl
             on l.lectureId = sl.lectureId
        """, nativeQuery = true)
    LectureDetailResponse getLectureById(Long lectureId);

    // 사용자 강의 정보 조회
    // 이건 userLecture 쪽으로 가도 될 듯
    @Query(value = """
            select *
            from userLecture
            where lectureId = :lectureId
            and userId = :userId;
        """, nativeQuery = true)
    UserLecture getUserLectureById(Long lectureId, Long userId);

    // 세부 강의 정보 조회
    @Query()
    List<SubLectureDetailResponse> getSubLectureById(Long lectureId);

    // 세부 강의 수강 정보 조회
//    @Query()
//    List<SubLectureDetailResponse> getUserLectureTimeById(Long lectureId, Long userId);

    // TODO : 강의 검색

    // 강의 구매


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
}
