package ssafy.d210.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.request.lecture.SubLectureRequest;
import ssafy.d210.backend.dto.request.payment.RatioRequest;
import ssafy.d210.backend.dto.request.quiz.QuizOptionRequest;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.entity.*;
import ssafy.d210.backend.enumeration.CategoryName;
import ssafy.d210.backend.repository.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LectureManagementServiceImpl implements LectureManagementService {

    private final LectureRepository lectureRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SubLectureRepository subLectureRepository;
    private final QuizRepository quizRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final PaymentRatioRepository paymentRatioRepository;
    private final UserLectureRepository userLectureRepository;
    private final UserLectureTimeRepository userLectureTimeRepository;

    @Override
    @Transactional
    public boolean registerLecture(LectureRegisterRequest request) {
        try {
            // 예외 처리
            // 한나 : 더 찾으면 알려주세요
            // 1. 필수 값 검증
            if (request.getTitle() == null || request.getTitle().isBlank()
                    || request.getCategoryName() == null
                    || request.getGoal() == null || request.getGoal().isBlank()
                    || request.getDescription() == null || request.getDescription().isBlank()
                    || request.getPrice() < 1) {
                throw new IllegalArgumentException("강의 필수 정보 누락, 가격 1미만");
            }

            // 2. 퀴즈 최소 3개 이상 등록
            if (request.getQuizzes() == null || request.getQuizzes().size() < 3 ) {
                throw new IllegalArgumentException("퀴즈는 최소 3개 이상 등록해야 합니다.");
            }

            // 3. SubLecture 최소 1개 이상 등록
            if (request.getSubLectures() == null || request.getSubLectures().isEmpty()) {
                throw new IllegalArgumentException("SubLecture은 최소 1개 이상 등록해야 합니다.");
            }

            // 4. Ratio 최소 1명 + 중복 이메일 금지 + 강의자 1명
            if (request.getRatios() == null || request.getRatios().isEmpty()) {
                throw new IllegalArgumentException("수익 분배는 최소 1명 이상 등록해야 합니다.");
            }
            Set<String> emailset = new HashSet<>();
            int lecturerCount = 0;
            for (RatioRequest ratioRequest : request.getRatios()) {
                if (!emailset.add(ratioRequest.getEmail())) {
                    throw new IllegalArgumentException("수익 분배 대상자 이메일이 중복 됐습니다. : " + ratioRequest.getEmail());
                }
                if (ratioRequest.isLecturer()) {
                    lecturerCount++;
                }
            }
            // 강의 등록자 무조건 1명
            if (lecturerCount != 1) {
                throw new IllegalArgumentException("등록자는 반드시 한 명이어야 하고, ratio에서 lecturer=true로 설정 되어야 한다.");
            }


            // 1. 카테고리 조회
            CategoryName categoryEnum = mapCategoryName(request.getCategoryName());
            Category category = categoryRepository.findByCategoryName(categoryEnum)
                    // 카테고리는 프론트에서 지정된 값만 줄거라 필요 없을 수도
                    .orElseThrow(() -> new RuntimeException("해당 카테고리가 없습니다 : " + categoryEnum));

            // 2. Lecture entity 생성
            Lecture lecture = new Lecture();
            lecture.setTitle(request.getTitle());
            lecture.setGoal(request.getGoal());
            lecture.setDescription(request.getDescription());
            lecture.setPrice(request.getPrice());
            lecture.setWalletKey(request.getWalletKey());
            lecture.setCategory(category);
            Lecture savedLecture = lectureRepository.save(lecture);

            // 3. SubLecture 저장
            List<SubLecture> subLectures = new ArrayList<>();
            for (SubLectureRequest subReq : request.getSubLectures()) {
                // 개별 강의 1초 이상이어야 한다.
                if (subReq.getSubLectureLength() <= 0) {
                    throw new IllegalArgumentException("개별 강의 길이는 1초 이상이어야 한다.");
                }
                SubLecture subLecture = new SubLecture();
                subLecture.setSubLectureTitle(subReq.getSubLectureTitle());
                subLecture.setSubLectureUrl(subReq.getSubLectureUrl());
                subLecture.setSubLectureLength(subReq.getSubLectureLength());
                subLecture.setLecture(savedLecture);
                subLectures.add(subLecture);
            }
            subLectureRepository.saveAll(subLectures);

            // 4. Quiz, QuizOption 저장 + 옵션은 3개만 허용 + 정답은 무조건 하나 + 퀴즈 옵션 내용 비어있으면 안된다.
            for (QuizRequest quizReq : request.getQuizzes()) {
                if (quizReq.getQuizOptions() == null || quizReq.getQuizOptions().size() != 3) {
                    throw new IllegalArgumentException("퀴즈 옵션은 정확히 3개만 등록해야 합니다.");
                }
                // 정답 개수 세기
                long correctCount = quizReq.getQuizOptions().stream()
                        .filter(QuizOptionRequest::getIsCorrect)
                        .count();

                if (correctCount != 1) {
                    throw new IllegalArgumentException("퀴즈 옵션에는 정확히 하나의 정답이 있어야 합니다.");
                }

                Quiz quiz = new Quiz();
                quiz.setQuestion(quizReq.getQuestion());
                quiz.setLecture(savedLecture);
                Quiz savedQuiz = quizRepository.save(quiz);

                List<QuizOption> quizOptions = new ArrayList<>();
                for (QuizOptionRequest optionReq : quizReq.getQuizOptions()) {
                    if (optionReq.getQuizOption() == null || optionReq.getQuizOption().isBlank()) {
                        throw new IllegalArgumentException(("퀴즈 옵션 내용은 비어 있을 수 없다."));
                    }
                    QuizOption quizOption = new QuizOption();
                    quizOption.setOptionText(optionReq.getQuizOption());
                    // true : 1, false : 0으로 변환
                    quizOption.setIsCorrect(optionReq.getIsCorrect() ? 1 : 0);
                    quizOption.setQuiz(savedQuiz);
                    quizOptions.add(quizOption);
                }
                quizOptionRepository.saveAll(quizOptions);
            }

            // 5. PaymentRatio + UserLecture + UserLectureTime 등록 : user email로 조회
            List<PaymentRatio> paymentRatios = new ArrayList<>();
            for (RatioRequest ratioReq : request.getRatios()) {
                User user = userRepository.findOptionalUserByEmail(ratioReq.getEmail())
                        .orElseThrow(() -> new RuntimeException("해당 이메일로 유저를 찾을 수 없습니다. : " + ratioReq.getEmail()));
                // PaymentRatio 저장
                PaymentRatio paymentRatio = new PaymentRatio();
                paymentRatio.setLecture(savedLecture);
                paymentRatio.setUser(user);
                paymentRatio.setRatio(ratioReq.getRatio());
                paymentRatio.setLecturer(ratioReq.isLecturer() ? 1 : 0);
                paymentRatios.add(paymentRatio);

                // UserLecture 등록
                UserLecture userLecture = new UserLecture();
                userLecture.setUser(user);
                userLecture.setLecture(savedLecture);
                UserLecture savedUserLecture = userLectureRepository.save(userLecture);

                // UserLectureTime 등록
                for (SubLecture sub : subLectures) {
                    UserLectureTime ult = new UserLectureTime();
                    ult.setUserLecture(savedUserLecture);
                    ult.setSubLecture(sub);
                    userLectureTimeRepository.save(ult);
                }
            }
            paymentRatioRepository.saveAll(paymentRatios);

            return true;
        } catch (Exception e) {
            // 워낙 예외 처리가 많아서 printStackTrace 적어둡니다.
            e.printStackTrace();
            return false;
        }
    }

        // 카테고리 이름 -> enum 매핑
        private CategoryName mapCategoryName(String input) {
            try {
                return CategoryName.valueOf(input);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 카테고리입니다. : " + input);
            }
        }
    }
