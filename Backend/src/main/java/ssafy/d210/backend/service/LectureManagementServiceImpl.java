package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ssafy.d210.backend.blockchain.AccountManager;
import ssafy.d210.backend.blockchain.ContractServiceFactory;
import ssafy.d210.backend.blockchain.RelayerAccount;
import ssafy.d210.backend.contracts.LectureSystem;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.request.quiz.QuizOptionRequest;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.entity.*;
import ssafy.d210.backend.enumeration.CategoryName;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.*;
import ssafy.d210.backend.redis.DistributedLock;
import ssafy.d210.backend.repository.*;
import ssafy.d210.backend.util.ResponseUtil;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
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
    private final ResponseUtil<Boolean> responseUtil;
    private final AccountManager accountManager;
    private final ContractServiceFactory contractServiceFactory;

    @Override
    @DistributedLock(key = "#registerLecture")
    @Transactional(rollbackFor = {Exception.class, BlockchainException.class, Throwable.class})
    public ResponseSuccessDto<Boolean> registerLecture(LectureRegisterRequest request) throws Exception {
        // 예외 발생 시 GlobalExceptionHandler, try-catch 제거
        isValidationForLecture(request);

        Set<String> emailset = new HashSet<>();
        final int[] lecturerCount = {0};
        request.getRatios().forEach(ratio -> {
            if (!emailset.add(ratio.getEmail())) {
                throw new DuplicatedValueException("수익 분배 대상자 이메일이 중복 됐습니다. : " + ratio.getEmail());
            }
            if (ratio.isLecturer()) {
                lecturerCount[0]++;
            }
        });

        // 강의 등록자 무조건 1명
        if (lecturerCount[0] != 1) {
            throw new InvalidRatioDataException("등록자는 반드시 한 명이어야 하고, ratio에서 lecturer=true로 설정 되어야 한다.");
        }


        // 1. 카테고리 조회
        CategoryName categoryEnum = mapCategoryName(request.getCategoryName());
        Category category = findCategory(categoryEnum);

        // 2. Lecture entity 생성
        Lecture lecture = new Lecture();
        lecture.setTitle(request.getTitle());
        lecture.setGoal(request.getGoal());
        lecture.setDescription(request.getDescription());
        lecture.setPrice(request.getPrice());
        lecture.setCID(request.getCID());
//            lecture.setWalletKey(aes256Util.encrypt(request.getWalletKey()));
        lecture.setCategory(category);
        Lecture savedLecture = lectureRepository.save(lecture);

        // 3. SubLecture 저장
        List<SubLecture> subLectures = request.getSubLectures().stream()
                .peek(subReq -> {
                    if (subReq.getSubLectureLength() <= 0) {
                        throw new InvalidLectureDataException("개별 강의 길이는 1초 이상이어야 한다.");
                    }
                })
                .map(subReq -> SubLecture.from(subReq, savedLecture))
                .collect(Collectors.toList());
        subLectureRepository.saveAll(subLectures);


        // 4. Quiz, QuizOption 저장 + 옵션은 3개만 허용 + 정답은 무조건 하나 + 퀴즈 옵션 내용 비어있으면 안된다.
        List<Quiz> savedQuizzes = request.getQuizzes().stream()
                .peek(this::isValidationForQuiz)
                .map(quizReq -> {
                    Quiz quiz = new Quiz();
                    quiz.setQuestion(quizReq.getQuestion());
                    quiz.setLecture(savedLecture);
                    return quiz;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        quizRepository::saveAll
                ));
        List<QuizOption> allQuizOptions = IntStream.range(0, savedQuizzes.size())
                .mapToObj(i -> {
                    Quiz savedQuiz = savedQuizzes.get(i);
                    QuizRequest quizReq = request.getQuizzes().get(i);

                    return quizReq.getQuizOptions().stream()
                            .peek(optionReq -> {
                                if (optionReq.getQuizOption() == null || optionReq.getQuizOption().isBlank()) {
                                    throw new InvalidQuizDataException("퀴즈 옵션 내용은 비어 있을 수 없다.");
                                }
                            })
                            .map(optionReq -> QuizOption.from(optionReq, savedQuiz));
                })
                .flatMap(stream -> stream)
                .collect(Collectors.toList());

        quizOptionRepository.saveAll(allQuizOptions);


        // 5. PaymentRatio + UserLecture + UserLectureTime 등록 : user email로 조회
        List<PaymentRatio> paymentRatios = new ArrayList<>(request.getRatios().size());
        List<UserLectureTime> userLectureTimes = new ArrayList<>();

        List<UserLecture> userLectures = request.getRatios().stream()
                .map(ratioReq -> {
                    User user = findUserEmail(ratioReq.getEmail());

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
                    userLecture.setReport(1);

                    return userLecture;

                })
                .collect(Collectors.toList());

        List<UserLecture> savedUserLectures = userLectureRepository.saveAll(userLectures);
        savedUserLectures.forEach(savedUserLecture -> {
            subLectures.forEach(sub -> {
                UserLectureTime ult = new UserLectureTime();
                ult.setUserLecture(savedUserLecture);
                ult.setSubLecture(sub);
                userLectureTimes.add(ult);
            });
        });
        paymentRatioRepository.saveAll(paymentRatios);
        userLectureTimeRepository.saveAll(userLectureTimes);

        // 블록체인에 강의를 등록하는 기능. 필요에 의해 앞쪽에 삽입될 수도 있습니다.
        RelayerAccount account = null;
        try {
            account = accountManager.acquireAccount();
            LectureSystem lectureSystem = contractServiceFactory.createLectureSystem(account);

            log.info("🚀 트랜잭션 보내는 relayer address: {}", account.getAddress());

            List<LectureSystem.Participant> participants = paymentRatios.stream().map(
                    paymentRatio -> new LectureSystem.Participant(
                            BigInteger.valueOf(paymentRatio.getUser().getId()),
                            BigInteger.valueOf(paymentRatio.getRatio())
                    )
            ).toList();

            RemoteFunctionCall<TransactionReceipt> tx = lectureSystem.createLecture(
                    BigInteger.valueOf(savedLecture.getId()),
                    request.getTitle(),
                    participants
            );
            TransactionReceipt receipt = tx.send();
            if (receipt.isStatusOK()) {
                log.info("Lecture created on blockchain with ID: {}", savedLecture.getId());
            } else {
                log.error("Blockchain transaction failed. Status: {}", receipt.getStatus());
                // 적절한 오류 처리
                throw new BlockchainException("블록체인 이슈로 강의등록 실패, 트랜잭션 롤백");
            }
        } finally {
            accountManager.releaseAccount(account);
        }

        return responseUtil.successResponse(true, HereStatus.SUCCESS_LECTURE_REGISTERED);

    }

    private void isValidationForQuiz(QuizRequest quizReq) {

        if (quizReq.getQuizOptions() == null || quizReq.getQuizOptions().size() != 3) {
            throw new InvalidQuizDataException("퀴즈 옵션은 정확히 3개만 등록해야 합니다.");
        }
        // 정답 개수 세기
        long correctCount = quizReq.getQuizOptions().stream()
                .filter(QuizOptionRequest::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new InvalidQuizDataException("퀴즈 옵션에는 정확히 하나의 정답이 있어야 합니다.");
        }
    }

    // 유효성 검사
    private void isValidationForLecture(LectureRegisterRequest request) {
        // 1. 필수 값 검증
        if (request.getTitle() == null || request.getTitle().isBlank()
                || request.getCategoryName() == null
                || request.getGoal() == null || request.getGoal().isBlank()
                || request.getDescription() == null || request.getDescription().isBlank()
                || request.getPrice() < 0) {
            throw new InvalidLectureDataException("강의 필수 정보 누락, 가격 음수");
        }

        // 2. 퀴즈 최소 3개 이상 등록
        if (request.getQuizzes() == null || request.getQuizzes().size() < 3 ) {
            throw new InvalidQuizDataException("퀴즈는 최소 3개 이상 등록해야 합니다.");
        }

        // 3. SubLecture 최소 1개 이상 등록
        if (request.getSubLectures() == null || request.getSubLectures().isEmpty()) {
            throw new InvalidLectureDataException("SubLecture은 최소 1개 이상 등록해야 합니다.");
        }

        // 4. Ratio 최소 1명 + 중복 이메일 금지 + 강의자 1명
        if (request.getRatios() == null || request.getRatios().isEmpty()) {
            throw new InvalidRatioDataException("수익 분배는 최소 1명 이상 등록해야 합니다.");
        }
    }

    @Transactional(readOnly = true)
    protected User findUserEmail(String email) {
        return userRepository.findOptionalUserByEmail(email)
                .orElseThrow(() -> new EntityIsNullException("해당 이메일로 유저를 찾을 수 없습니다. : " + email));
    }

    @Transactional(readOnly = true)
    protected Category findCategory(CategoryName categoryEnum) {
        return categoryRepository.findByCategoryName(categoryEnum)
                // 카테고리는 프론트에서 지정된 값만 줄거라 필요 없을 수도
                .orElseThrow(() -> new EntityIsNullException("해당 카테고리가 없습니다 : " + categoryEnum));
    }

    // 카테고리 이름 -> enum 매핑
    private CategoryName mapCategoryName(String input) {
        try {
            return CategoryName.valueOf(input);
        } catch (IllegalArgumentException e) {
            throw new InvalidLectureDataException("유효하지 않은 카테고리입니다. : " + input);
        }
    }
}