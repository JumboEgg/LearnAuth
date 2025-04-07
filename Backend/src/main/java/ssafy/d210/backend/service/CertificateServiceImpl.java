package ssafy.d210.backend.service;
//
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateToken;
import ssafy.d210.backend.entity.Lecture;
import ssafy.d210.backend.entity.PaymentRatio;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.DefaultException;
import ssafy.d210.backend.exception.service.BlockchainException;
import ssafy.d210.backend.exception.service.EntityIsNullException;
import ssafy.d210.backend.repository.LectureRepository;
import ssafy.d210.backend.repository.PaymentRatioRepository;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CertificateServiceImpl implements CertificateService{
    private final UserLectureRepository userLectureRepository;
    private final ResponseUtil responseUtil;
    private final LectureSystem lectureSystem;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final PaymentRatioRepository paymentRatioRepository;

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
    public BigInteger issueCertificate(Long userId, String cid) {
        try {
            CompletableFuture<TransactionReceipt> future = issueCertificateToContract(userId, cid);
            TransactionReceipt receipt = future.get();
            return extractTokenIdFromReceipt(receipt);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ResponseSuccessDto<CertificateToken> saveCertificate(BigInteger tokenId, Long lectureId, Long userId) {
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

        CertificateToken certificateToken = CertificateToken.builder()
                .token(tokenId)
                .build();

        log.info("Certificate saved on userLectureId {} value {}", userLecture.getId(), token);
        return responseUtil.successResponse(certificateToken, HereStatus.SUCCESS_CERTIFICATE_OWN);
    }

    @Transactional(readOnly = true)
    protected Optional<UserLecture> findByUserIdAndLectureId(Long userId, Long lectureId) {
        return userLectureRepository.findByUserIdAndLectureId(userId, lectureId);
    }


    public CompletableFuture<TransactionReceipt> issueCertificateToContract(Long userId, String cid) {
        try {
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
            List<LectureSystem.NFTIssuedEventResponse> events = lectureSystem
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
