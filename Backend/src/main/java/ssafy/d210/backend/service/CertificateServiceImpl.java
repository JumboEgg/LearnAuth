package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ssafy.d210.backend.blockchain.AccountManager;
import ssafy.d210.backend.blockchain.ContractServiceFactory;
import ssafy.d210.backend.blockchain.RelayerAccount;
import ssafy.d210.backend.contracts.LectureSystem;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateToken;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.BlockchainException;
import ssafy.d210.backend.exception.service.EntityIsNullException;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.util.ResponseUtil;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CertificateServiceImpl implements CertificateService{
    private final UserLectureRepository userLectureRepository;
    private final ResponseUtil responseUtil;
    private final AccountManager accountManager;
    private final ContractServiceFactory contractServiceFactory;

    @Override
    @Transactional
    public ResponseSuccessDto<List<CertificateResponse>> getCertificates(Long userId) {
        // 사용자 ID로 완료된 강의 조회
        List<CertificateResponse> certificates = finishedUserLecture(userId);

        log.info("User {} certificates: {}", userId, certificates);

        // 결과 검증 및 로깅
        if (certificates.isEmpty()) {
            log.warn("No certificates found for user {}", userId);
        }

        return responseUtil.successResponse(certificates, HereStatus.SUCCESS_CERTIFICATE);
    }

    @Transactional(readOnly = true)
    protected List<CertificateResponse> finishedUserLecture(Long userId) {
        return userLectureRepository.getFinishedUserLecture(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseSuccessDto<CertificateDetailResponse> getCertificatesDetail(Long userId, Long lectureId) {
        // 이수증 상세 정보 조회
        CertificateDetailResponse certificateDetail = getCertificateDetail(userId, lectureId);

        // 조회 결과 null 체크
        if (certificateDetail == null) {
            log.error("No certificate detail found for user {} and lecture {}", userId, lectureId);
        } else {
            log.info("Certificate Detail: title={}, teacherName={}, certificateDate={}",
                    certificateDetail.getTitle(),
                    certificateDetail.getTeacherName(),
                    certificateDetail.getCertificateDate());
        }

        return responseUtil.successResponse(certificateDetail, HereStatus.SUCCESS_CERTIFICATE_DETAIL);
    }

    @Transactional(readOnly = true)
    protected CertificateDetailResponse getCertificateDetail(Long userId, Long lectureId) {
        return userLectureRepository.getCertificateDetail(userId, lectureId);
    }

    @Override
    // 수료증 발급 try catch -> 예외 throw
    public BigInteger issueCertificate(Long userId, String cid) throws Exception {
        CompletableFuture<TransactionReceipt> future = issueCertificateToContract(userId, cid);
        try {
            // 트랜잭션 타임아웃 설정 (2분)
            TransactionReceipt receipt = future.get(2, TimeUnit.MINUTES);
            return extractTokenIdFromReceipt(receipt);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("Transaction timed out for user {}: {}", userId, e.getMessage());
            throw new BlockchainException("Transaction timed out after 2 minutes", e);
        }
    }

    @Override
    public ResponseSuccessDto<CertificateToken> saveCertificate(BigInteger tokenId, Long lectureId, Long userId, String cid) {

        UserLecture userLecture = findByUserIdAndLectureId(userId, lectureId)
                .orElseThrow(() -> {
                    log.error("UserLecture with userId {} and lectureId {} doesn't exist", userId, lectureId);
                    return new EntityIsNullException("UserLecture not found");
                });

        if (tokenId == null || userLecture == null) {
            log.error("UserLecture with userId {} and lectureId {} doesn't exist", userId, lectureId);
            return responseUtil.successResponse(false, HereStatus.SUCCESS_CERTIFICATE_OWN);
        }
        int token = tokenId.intValue();
        userLecture.setCertificate(token);
        userLecture.setQrCode(cid);
        CertificateToken certificateToken = CertificateToken.builder()
                .token(token)
                .build();

        log.info("Certificate saved on userLectureId {} value {}", userLecture.getId(), tokenId);
        return responseUtil.successResponse(certificateToken, HereStatus.SUCCESS_CERTIFICATE_OWN);
    }

    @Transactional(readOnly = true)
    protected Optional<UserLecture> findByUserIdAndLectureId(Long userId, Long lectureId) {
        return userLectureRepository.findByUserIdAndLectureId(userId, lectureId);
    }


    public CompletableFuture<TransactionReceipt> issueCertificateToContract(Long userId, String cid) {
        RelayerAccount account = null;
        try {
            account = accountManager.acquireAccount();
            LectureSystem lectureSystem = contractServiceFactory.createLectureSystem(account);

            log.info("🚀 트랜잭션 보내는 relayer address: {}", account.getAddress());
            log.info("Issue NFT to userId {} with cid {}", userId, cid);
            // NFT 생성
            log.info("Executing issueCertificate");
            return lectureSystem.issueNFT(BigInteger.valueOf(userId), cid).sendAsync()
                    .thenApply(receipt -> {
                        log.info("issueCertificate transaction completed. Hash: {}", receipt.getTransactionHash());
                        return receipt;
                    })
                    .exceptionally(ex -> {
                        log.error("Error while issuing NFT: {}", ex.getMessage(), ex);
                        throw new RuntimeException("Token deposit failed", ex);
                    });

        } catch (Exception e) {
            log.error("Error in depositToken: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deposit token", e);
        } finally {
            accountManager.releaseAccount(account);
        }
    }


    /**
     * Extracts the token ID from the NFTIssued event in the transaction receipt
     *
     * @param receipt The transaction receipt
     * @return The token ID as a BigInteger
     * @throws BlockchainException if the event cannot be found or parsed
     */
    private BigInteger extractTokenIdFromReceipt(TransactionReceipt receipt) {
        try {
            // Extract events from the transaction receipt
            List<LectureSystem.NFTIssuedEventResponse> events = LectureSystem
                    .getNFTIssuedEvents(receipt);

            if (events.isEmpty()) {
                throw new BlockchainException("NFTIssued event not found in transaction");
            }

            // Return the token ID from the first event
            return events.get(0).tokenId;
        } catch (Exception e) {
            log.error("Error extracting token ID from receipt: {}", e.getMessage());
            throw new BlockchainException("Failed to extract token ID from transaction: " + e.getMessage(), e);
        }
    }
}
