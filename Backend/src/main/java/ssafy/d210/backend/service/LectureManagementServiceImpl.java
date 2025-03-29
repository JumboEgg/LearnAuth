package ssafy.d210.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.request.lecture.SubLectureRequest;
import ssafy.d210.backend.dto.request.payment.RatioRequest;
import ssafy.d210.backend.dto.request.quiz.QuizOptionRequest;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.entity.*;
import ssafy.d210.backend.enumeration.CategoryName;
import ssafy.d210.backend.repository.*;

import java.util.ArrayList;
import java.util.List;

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

            // 4. Ratio 최소 1명 + 강의 등록자 포함
            if (request.getRatios() == null || request.getRatios().isEmpty()) {
                throw new IllegalArgumentException("수익 분배는 최소 1명 이상 등록해야 합니다.");
            }
            boolean hasLecturer = request.getRatios().stream().anyMatch(RatioRequest::isLecturer);
            if (!hasLecturer) {
                throw new IllegalArgumentException("등록자는 반드시 ratio에 포함되어야 한다.");
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
                SubLecture subLecture = new SubLecture();
                subLecture.setSubLectureTitle(subReq.getSubLectureTitle());
                subLecture.setSubLectureUrl(subReq.getSubLectureUrl());
                subLecture.setSubLectureLength(subReq.getSubLectureLength());
                subLecture.setLecture(savedLecture);
                subLectures.add(subLecture);
            }
            subLectureRepository.saveAll(subLectures);

            // 4. Quiz, QuizOption 저장
            for (QuizRequest quizReq : request.getQuizzes()) {
                Quiz quiz = new Quiz();
                quiz.setQuestion(quizReq.getQuestion());
                quiz.setLecture(savedLecture);
                Quiz savedQuiz = quizRepository.save(quiz);
                List<QuizOption> quizOptions = new ArrayList<>();
                for (QuizOptionRequest optionReq : quizReq.getQuizOptions()) {
                    QuizOption quizOption = new QuizOption();
                    quizOption.setOptionText(optionReq.getQuizOption());
                    // true : 1, false : 0으로 변환
                    quizOption.setIsCorrect(optionReq.getIsCorrect() ? 1 : 0);
                    quizOption.setQuiz(savedQuiz);
                    quizOptions.add(quizOption);
                }
                quizOptionRepository.saveAll(quizOptions);
            }

            // 5. PaymentRatio 저장 : user email로 조회
            List<PaymentRatio> paymentRatios = new ArrayList<>();
            for (RatioRequest ratioReq : request.getRatios()) {
                User user = userRepository.findOptionalUserByEmail(ratioReq.getEmail())
                        .orElseThrow(() -> new RuntimeException("해당 이메일로 유저를 찾을 수 없습니다. : " + ratioReq.getEmail()));
                PaymentRatio paymentRatio = new PaymentRatio();
                paymentRatio.setLecture(savedLecture);
                paymentRatio.setUser(user);
                paymentRatio.setRatio(ratioReq.getRatio());
                paymentRatio.setLecturer(ratioReq.isLecturer() ? 1 : 0);
                paymentRatios.add(paymentRatio);
            }
            paymentRatioRepository.saveAll(paymentRatios);

            return true;
        } catch (Exception e) {
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
