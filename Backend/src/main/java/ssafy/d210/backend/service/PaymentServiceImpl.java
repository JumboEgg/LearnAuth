package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import ssafy.d210.backend.contracts.CATToken;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.contracts.LectureSystem;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService{

    private final Web3j web3j;
    private final Credentials credentials;
    private final LectureForwarder lectureForwarder;
    private final LectureSystem lectureSystem;
    private final CATToken catToken;
    private final BigInteger BC_ETHER = BigInteger.valueOf((long) Math.pow(10, 18));

    @Override
    public ResponseSuccessDto<Boolean> increaseToken(long userId, int quantity) {
        ResponseSuccessDto<Boolean> res;
        try {
            CompletableFuture<TransactionReceipt> tx = depositToken(userId, quantity);
            TransactionReceipt receipt = tx.get();
            log.info("Token deposit transaction success : {}", receipt);
            res = new ResponseSuccessDto<>(true);
        } catch (Exception e) {
            log.info("Token deposit transaction failed. userId: {}, quantity: {}", userId, quantity);
            res = new ResponseSuccessDto<>(false);
        }
        return res;
    }

    /**
     * 사용자에게 토큰을 입금합니다.
     *
     * @param userId 사용자 ID
     * @param amount 입금할 토큰 양 (18자리 소수점을 포함한 값)
     * @return 트랜잭션 영수증
     */
    public CompletableFuture<TransactionReceipt> depositToken(Long userId, int amount) {
        try {
            log.info("Depositing {} tokens to user {}", Convert.fromWei((BigInteger.valueOf(amount).multiply(BC_ETHER)).toString(), Convert.Unit.ETHER).toPlainString(), userId);

            // LectureSystem 컨트랙트에 토큰 사용 승인
            log.info("Approving token usage for lecture system");
            TransactionReceipt approvalReceipt = catToken.approve(lectureSystem.getContractAddress(), BigInteger.valueOf(amount)).send();
            log.info("Token approval transaction hash: {}", approvalReceipt.getTransactionHash());

            // 토큰 입금 실행
            log.info("Executing deposit transaction");
            return lectureSystem.depositToken(BigInteger.valueOf(userId), BigInteger.valueOf(amount)).sendAsync()
                    .thenApply(receipt -> {
                        log.info("Deposit transaction completed. Hash: {}", receipt.getTransactionHash());
                        return receipt;
                    })
                    .exceptionally(ex -> {
                        log.error("Error while depositing token: {}", ex.getMessage(), ex);
                        throw new RuntimeException("Token deposit failed", ex);
                    });

        } catch (Exception e) {
            log.error("Error in depositToken: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deposit token", e);
        }
    }
}
