package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.datatypes.Int;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.lecture.*;
import ssafy.d210.backend.entity.*;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.*;
import ssafy.d210.backend.util.ResponseUtil;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LectureServiceImpl implements LectureService{

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final SubLectureRepository subLectureRepository;
    private final UserLectureRepository userLectureRepository;
    private final UserLectureTimeRepository userLectureTimeRepository;
    private final ResponseUtil responseUtil;

    @Override
    @Transactional
    @Cacheable(value = "lectureCategory", cacheManager = "redisCacheManager")
    public ResponseSuccessDto<List<LectureInfoListResponse>> getLecturesByCategory(int categoryId, int page) {
        // 카테고리별 강의 목록 조회
        int offset = (page - 1) * 12;
        List<LectureInfoListResponse> lectures = findLectureByCategory(categoryId, offset);

        log.info("Category {} page {} lectures: {}", categoryId, page, lectures);

        // 결과 검증 및 로깅
        if (lectures.isEmpty()) {
            log.warn("No lectures found for category {}", categoryId);
        }

        return responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE_CATEGORY);

    }
    @Transactional(readOnly = true)
    protected List<LectureInfoListResponse> findLectureByCategory(int categoryId, int offset) {
        return lectureRepository.getLecturesByCategory(categoryId, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseSuccessDto<RecommendedLectureResponse> getRecommendedLectures() {
        // 비동기 처리
        CompletableFuture<List<LectureInfoListResponse>> mostCompletedLectures =
                CompletableFuture.supplyAsync(lectureRepository::getMostFinishedLectures);

        CompletableFuture<List<LectureInfoListResponse>> randomLectures =
                CompletableFuture.supplyAsync(lectureRepository::getRandomLectures);

        CompletableFuture<List<LectureInfoListResponse>> recentLectures =
                CompletableFuture.supplyAsync(lectureRepository::getNewestLectures);

        CompletableFuture.allOf(mostCompletedLectures, randomLectures, recentLectures);

        RecommendedLectureResponse lectures = new RecommendedLectureResponse();
        lectures.setMostCompletedLectures(mostCompletedLectures.join());
        lectures.setRandomLectures(randomLectures.join());
        lectures.setRecentLectures(recentLectures.join());

        return responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lectureMostCompleted", cacheManager = "redisCacheManager")
    public ResponseSuccessDto<List<LectureInfoListResponse>> getMostCompletedLectures() {
        List<LectureInfoListResponse> mostCompletedLectures = lectureRepository.getMostFinishedLectures();
        return responseUtil.successResponse(mostCompletedLectures, HereStatus.SUCCESS_LECTURE);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseSuccessDto<List<LectureInfoListResponse>> getRandomLectures() {
        List<LectureInfoListResponse> randomLectures = lectureRepository.getRandomLectures();
        return responseUtil.successResponse(randomLectures, HereStatus.SUCCESS_LECTURE);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lectureRecent", cacheManager = "redisCacheManager")
    public ResponseSuccessDto<List<LectureInfoListResponse>> getMostRecentLectures() {
        List<LectureInfoListResponse> newestLectures = lectureRepository.getNewestLectures();
        return responseUtil.successResponse(newestLectures, HereStatus.SUCCESS_LECTURE);
    }


    @Override
    @Transactional
    public ResponseSuccessDto<LectureDetailResponse> getLectureDetail(Long lectureId, Long userId) {

        LectureDetail lecture = findLectureById(lectureId);

        log.info("result: {}", lecture.getTitle());

        if (lecture == null) {
            log.warn("No lecture found for lectureId {}", lectureId);
            return responseUtil.successResponse(null, HereStatus.SUCCESS_LECTURE_DETAIL);
        }

        LectureDetailResponse lectureDetail = convertToLectureDetailResponse(lecture);
//        LectureDetailResponse lectureDetail = LectureDetailResponse.builder().build();
        CompletableFuture<List<Integer>> subLectureIdListFuture = CompletableFuture.supplyAsync(() ->
                lectureRepository.getSublecturesById(lectureId));
        CompletableFuture<UserLecture> userLectureFuture = CompletableFuture.supplyAsync(() ->
                userLectureRepository.getUserLectureById(lectureId, userId));

        CompletableFuture.allOf(subLectureIdListFuture, userLectureFuture);

        List<Integer> subLectureIdList = subLectureIdListFuture.join();
        UserLecture userLecture = userLectureFuture.join();

        if (userLecture == null) {
            log.warn("No userLecture found for lectureId {} and userId {}", lectureId, userId);
        } else {
            lectureDetail.setUserLectureId(userLecture.getId());
            lectureDetail.setRecentLectureId(userLecture.getRecentLectureId());
            lectureDetail.setOwned(true);
            lectureDetail.setCertificate(userLecture.getCertificateDate() != null);
        }

        if (subLectureIdList.isEmpty()) {
            log.warn("No subLecture found for lectureId {}", lectureId);
        } else if (userLecture != null){
            List<SubLectureDetailResponse> subLectureDetail = findUserLectureTime(subLectureIdList, userLecture.getId());

            if (subLectureDetail.isEmpty()) log.warn("No userLectureTime found for subLectureIdList {}", subLectureIdList);
            lectureDetail.setSubLectures(subLectureDetail);
            log.info("subLectureDetail: {}", subLectureDetail.get(0));
        } else {
            List<SubLecture> subLectures = findSubLecture(lectureId);

            List<SubLectureDetailResponse> subLectureDetail = IntStream.range(0, subLectures.size())
                    .mapToObj(index -> {
                        SubLecture subLecture = subLectures.get(index);
                        return new SubLectureDetailResponse(
                                subLecture.getId(),
                                subLecture.getSubLectureTitle(),
                                subLecture.getSubLectureUrl(),
                                subLecture.getSubLectureLength(),
                                index + 1L,
                                null,
                                0
                        );
                    })
                    .collect(Collectors.toList());
            lectureDetail.setSubLectures(subLectureDetail);

        }

        int studentCount = FindCountUserLectureByLectureId(lectureId);

        lectureDetail.setStudentCount(studentCount);

        log.info("Lecture Detail : {}", lectureDetail);

        return responseUtil.successResponse(lectureDetail, HereStatus.SUCCESS_LECTURE_DETAIL);
    }

    @Transactional(readOnly = true)
    protected int FindCountUserLectureByLectureId(Long lectureId) {
        return userLectureRepository.countUserLectureByLectureId(lectureId);
    }

    @Transactional(readOnly = true)
    protected List<SubLectureDetailResponse> findUserLectureTime(List<Integer> subLectureIdList, Long userLectureId) {
        return lectureRepository.getUserLectureTime(subLectureIdList, userLectureId);
    }

    @Transactional(readOnly = true)
    protected LectureDetail findLectureById(Long lectureId) {
        return lectureRepository.getLectureById(lectureId);
    }

    @Override
    @Transactional
    public ResponseSuccessDto<LectureSearchResponse> searchLectures(String keyword, int page) {
        int pageSize = 12;

        // 전체 결과 수
        int total = lectureRepository.countLecturesByKeyword(keyword);

        // pageable 객체 생성
        Pageable pageable = PageRequest.of(page-1, pageSize);

        // 페이징 처리된 검색 결과 조회
        List<LectureInfoResponse> pagedList = findSearch(keyword, pageable);


        LectureSearchResponse response = new LectureSearchResponse();
        response.setTotalResults(total);
        response.setCurrentPage(page);
        response.setSearchResults(pagedList);
        return responseUtil.successResponse(response, HereStatus.SUCCESS_LECTURE_SEARCH);
    }

    @Transactional(readOnly = true)
    protected List<LectureInfoResponse> findSearch(String keyword, Pageable pageable) {
        return lectureRepository.searchLecturesByKeywordPaged(keyword, pageable);
    }

    @Override
    public ResponseSuccessDto<Object> purchaseLecture(Long userId, Long lectureId) {
        UserLecture userLecture = new UserLecture();
        Optional<User> user = findUser(userId);

        Optional<Lecture> lecture = findLecture(lectureId);
        SubLecture subLec = findFirstByLectureId(lectureId);

        userLecture.createUserLecture(user.get(), lecture.get(), subLec.getId());

        UserLecture savedUserLecture = findUserLecture(userLecture);

        List<SubLecture> subLectureList = findSubLecture(lectureId);

        List<UserLectureTime> userLectureTimes = subLectureList.stream()
                .map(subLecture -> {
                    UserLectureTime userLectureTime = new UserLectureTime();
                    userLectureTime.createUserLectureTime(savedUserLecture, subLecture);
                    return userLectureTime;
                })
                .collect(Collectors.toList());

        userLectureTimeRepository.saveAll(userLectureTimes);

        return responseUtil.successResponse("ok", HereStatus.SUCCESS_LECTURE_BUY);
    }

    @Transactional(readOnly = true)
    protected SubLecture findFirstByLectureId(Long lectureId) {
        return subLectureRepository.findFirstByLectureId(lectureId);
    }

    @Transactional(readOnly = true)
    protected Optional<Lecture> findLecture(Long lectureId) {
        return lectureRepository.findById(lectureId);
    }

    @Transactional(readOnly = true)
    protected Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    protected List<SubLecture> findSubLecture(Long lectureId) {
        return subLectureRepository.findSubLectureByLectureIdOrderById(lectureId);
    }

    @Transactional(readOnly = true)
    protected UserLecture findUserLecture(UserLecture userLecture) {
        return userLectureRepository.save(userLecture);
    }

    @Override
    @Transactional
    public ResponseSuccessDto<List<LectureResponse>> getPurchasedLectures(Long userId) {
        // 사용자 ID로 구매한 강의 목록 조회
        List<LectureProfile> lectureProfiles = findPurchasedLectures(userId);

        log.info("UserId: {} lectures: {}", userId, lectureProfiles);

        // 결과 검증 및 로깅
        if (lectureProfiles.isEmpty()) {
            log.warn("No lectures found for userId {}", userId);
        }

        List<LectureResponse> lectures = lectureProfiles.stream()
                .map(this::convertToLectureResponse)
                .toList();

        return responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
    }

    @Transactional(readOnly = true)
    protected List<LectureProfile> findPurchasedLectures(Long userId) {
        return lectureRepository.getPurchasedLectures(userId);
    }

    @Override
    @Transactional
    public ResponseSuccessDto<List<LectureResponse>> getParticipatedLectures(Long userId) {
        // 사용자가 참여한 강의 목록 조회
        List<LectureProfile> lectureProfiles = findParticipatedLectures(userId);

        log.info("UserId: {} lectures: {}", userId, lectureProfiles);

        // 결과 검증 및 로깅
        if (lectureProfiles.isEmpty()) {
            log.warn("No lectures found for userId {}", userId);
        }

        List<LectureResponse> lectures = lectureProfiles.stream()
                .map(this::convertToLectureResponse)
                .toList();

        return responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
    }

    @Transactional(readOnly = true)
    protected List<LectureProfile> findParticipatedLectures(Long userId) {
        return lectureRepository.getParticipatedLectures(userId);
    }

    private LectureDetailResponse convertToLectureDetailResponse(LectureDetail lectureDetail) {
        return LectureDetailResponse.builder()
                .lecturer(lectureDetail.getLecturer())
                .lectureUrl(lectureDetail.getLectureUrl())
                .price(lectureDetail.getPrice())
                .goal(lectureDetail.getGoal())
                .title(lectureDetail.getTitle())
                .description(lectureDetail.getDescription())
                .lectureId(lectureDetail.getLectureId())
                .categoryName(lectureDetail.getCategoryName())
                .CID(lectureDetail.getCID())
                .build();
    }

    // 병렬처리
    private LectureResponse convertToLectureResponse(LectureProfile profile) {

        CompletableFuture<Integer> subLectureCountFuture = CompletableFuture.supplyAsync(() ->
                subLectureRepository.countSubLecturesByLectureId(profile.getLectureId()));
        CompletableFuture<Integer> finishedSubLectureCountFuture = CompletableFuture.supplyAsync(() ->
                userLectureTimeRepository.countUserLectureTimesByLectureId(profile.getLectureId()));
        CompletableFuture<SubLecture> subLectureUrlFuture = CompletableFuture.supplyAsync(() ->
                subLectureRepository.findFirstByLectureId(profile.getLectureId()));
        CompletableFuture.allOf(subLectureCountFuture, finishedSubLectureCountFuture, subLectureUrlFuture);

        int subLectureCount = subLectureCountFuture.join();
        int finishedSubLectureCount = finishedSubLectureCountFuture.join();
        SubLecture subLectureUrl = subLectureUrlFuture.join();

        return LectureResponse.builder()
                .isLecturer(profile.getIsLecturer())
                .title(profile.getTitle())
                .lecturer(profile.getLecturer())
                .categoryName(profile.getCategoryName())
                .lectureId(profile.getLectureId())
                .learningRate(subLectureCount * 1.0 / finishedSubLectureCount)
                .lectureUrl(subLectureUrl.getSubLectureUrl())
                .build();
    }
}
