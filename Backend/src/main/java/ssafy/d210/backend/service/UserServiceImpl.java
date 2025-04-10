package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import ssafy.d210.backend.blockchain.AccountManager;
import ssafy.d210.backend.blockchain.ContractServiceFactory;
import ssafy.d210.backend.blockchain.RelayerAccount;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.contracts.LectureSystem;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.SignupResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.BlockchainException;
import ssafy.d210.backend.exception.service.DuplicatedValueException;
import ssafy.d210.backend.exception.service.PasswordIsNotAllowed;
import ssafy.d210.backend.redis.DistributedLock;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountManager accountManager;
    private final ContractServiceFactory contractServiceFactory;


    @Override
    @Transactional(rollbackFor = {Exception.class, PasswordIsNotAllowed.class, BlockchainException.class})
    @DistributedLock(key = "#userSignupRequest.email")
    public ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest) throws Exception {

        // 이름 본인인증
        User newUser = new User();
        newUser.createUser(userSignupRequest);

        checkDuplication(newUser.getEmail() ,newUser.getNickname());

        if (userSignupRequest.getPassword().length() < 8 || userSignupRequest.getPassword().length() > 100) {
            log.error("비밀번호가 8자보다 작거나 100자 보다 큽니다.");
            throw new PasswordIsNotAllowed("비밀번호가 8자보다 작거나 100자 보다 큽니다.");
        }

        newUser.setPassword(bCryptPasswordEncoder.encode(userSignupRequest.getPassword()));
        User user = userRepository.save(newUser);


        addUserToContract(user.getId(), userSignupRequest.getWallet());


        SignupResponse userSignupResponse = SignupResponse.builder()
                .nickname(newUser.getNickname())
                .message("회원가입이 완료되었습니다.")
                .build();
        return responseUtil.successResponse(userSignupResponse, HereStatus.SUCCESS_SIGNUP);
    }

    @Transactional(readOnly = true)
    protected void checkDuplication(String email, String nickname) {
        boolean emailExists = userRepository.existsByEmail(email);
        if (emailExists) {
            log.error("중복 이메일: {}", email);
            throw new DuplicatedValueException("이미 사용중인 이메일입니다.");
        }

        boolean nicknameExists = userRepository.existsByNickname(nickname);
        if (nicknameExists) {
            log.error("중복 닉네임: {}", nickname);
            throw new DuplicatedValueException("이미 사용중인 닉네임입니다.");
        }
    }

    // Lecture System 컨트랙트에 사용자 지갑 등록
    private TransactionReceipt addUserToContract(Long userId, String userAddress) {
        RelayerAccount account = null;
        try {
            account = accountManager.acquireAccount();
            LectureSystem lectureSystem = contractServiceFactory.createLectureSystem(account);

            log.info("Adding user with userId {} and wallet address {} to blockchain", userId, userAddress);

            BigInteger userIdBigInt = BigInteger.valueOf(userId);

            CompletableFuture<TransactionReceipt> future = lectureSystem
                    .addUser(userIdBigInt, userAddress)
                    .sendAsync();

            TransactionReceipt receipt = future.get();
            log.info("User with userId {} added successfully. Transaction Hash: {}", userId, receipt.getTransactionHash());
            return receipt;
        } catch (Exception e) {
            log.error("Transaction failed : ", e);
            throw new BlockchainException("블록체인 사용자 등록 실패", e);
        } finally {
            accountManager.releaseAccount(account);
        }
    }
}
