package ssafy.d210.backend.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import ssafy.d210.backend.dto.common.ResponseSuccessDto;
//import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
//import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
//import ssafy.d210.backend.dto.response.certificate.CertificateToken;
//import ssafy.d210.backend.entity.UserLecture;
//import ssafy.d210.backend.enumeration.response.HereStatus;
//import ssafy.d210.backend.repository.UserLectureRepository;
//import ssafy.d210.backend.util.ResponseUtil;
//
//import java.math.BigInteger;
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.mockito.Mockito.*;
//
@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {
//
//    @Mock
//    private UserLectureRepository userLectureRepository;
//
//    @Mock
//    private ResponseUtil responseUtil;
//
//    @InjectMocks
//    private CertificateServiceImpl certificateService;
//
//    private CertificateResponse dummyCertificate;
//    private CertificateDetailResponse dummyCertificateDetail;
//
//    @BeforeEach
//    void setup() {
//        dummyCertificate = new CertificateResponse(1L, "Spring 강의", "웹 개발", 123, new Date());
//        dummyCertificateDetail = CertificateDetailResponse.builder()
//                .title("스프링 강의")
//                .teacherName("김개발")
//                .teacherWallet("0xABC123")
//                .certificateDate((java.sql.Date) new Date())
//                .certificate(123)
//                .qrCode("cid123")
//                .build();
//    }
//
//    @Test
//    @DisplayName("사용자 이수증 전체 조회 성공")
//    void getCertificatesTest() {
//        Long userId = 1L;
//        List<CertificateResponse> mockList = List.of(dummyCertificate);
//
//        when(userLectureRepository.getFinishedUserLecture(userId)).thenReturn(mockList);
//
//        ResponseSuccessDto<List<CertificateResponse>> expected =
//                new ResponseSuccessDto<>(null, 200, HereStatus.SUCCESS_CERTIFICATE.name(), mockList);
//
//        when(responseUtil.successResponse(mockList, HereStatus.SUCCESS_CERTIFICATE)).thenReturn(expected);
//
//        ResponseSuccessDto<List<CertificateResponse>> result = certificateService.getCertificates(userId);
//
//        assertThat(result.getData().size()).isEqualTo(1);
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_CERTIFICATE.name());
//    }
//
//    @Test
//    @DisplayName("이수증 상세 정보 조회 성공")
//    void getCertificatesDetailTest() {
//        Long userId = 1L;
//        Long lectureId = 10L;
//
//        when(userLectureRepository.getCertificateDetail(userId, lectureId)).thenReturn(dummyCertificateDetail);
//
//        ResponseSuccessDto<CertificateDetailResponse> expected =
//                new ResponseSuccessDto<>(null, 200, HereStatus.SUCCESS_CERTIFICATE_DETAIL.name(), dummyCertificateDetail);
//
//        when(responseUtil.successResponse(dummyCertificateDetail, HereStatus.SUCCESS_CERTIFICATE_DETAIL)).thenReturn(expected);
//
//        ResponseSuccessDto<CertificateDetailResponse> result = certificateService.getCertificatesDetail(userId, lectureId);
//
//        assertThat(result.getData().getTitle()).isEqualTo("스프링 강의");
//    }
//
//    @Test
//    @DisplayName("이수증 저장 성공")
//    void saveCertificateTest() {
//        Long userId = 1L;
//        Long lectureId = 10L;
//        Integer token = 888;
//        String cid = "cid888";
//
//        UserLecture userLecture = new UserLecture();
//        userLecture.setId(77L);
//
//        when(userLectureRepository.findByUserIdAndLectureId(userId, lectureId))
//                .thenReturn(Optional.of(userLecture));
//
//        CertificateToken responseToken = CertificateToken.builder().token(token).build();
//
//        ResponseSuccessDto<CertificateToken> expected =
//                new ResponseSuccessDto<>(null, 200, HereStatus.SUCCESS_CERTIFICATE_OWN.name(), responseToken);
//
//        when(responseUtil.successResponse(responseToken, HereStatus.SUCCESS_CERTIFICATE_OWN)).thenReturn(expected);
//
//        ResponseSuccessDto<CertificateToken> result = certificateService.saveCertificate(
//                BigInteger.valueOf(token), lectureId, userId, cid
//        );
//
//        assertThat(result.getData().getToken()).isEqualTo(token);
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_CERTIFICATE_OWN.name());
//    }
//
//    @Test
//    @DisplayName("이수증 저장 실패 - 유저 강의 정보 없음")
//    void saveCertificateFailWhenUserLectureMissing() {
//        Long userId = 1L;
//        Long lectureId = 999L;
//        BigInteger tokenId = BigInteger.valueOf(888);
//        String cid = "cidX";
//
//        when(userLectureRepository.findByUserIdAndLectureId(userId, lectureId))
//                .thenReturn(Optional.empty());
//
//        ResponseSuccessDto<Boolean> expected =
//                new ResponseSuccessDto<>(null, 200, HereStatus.SUCCESS_CERTIFICATE_OWN.name(), false);
//
//        when(responseUtil.successResponse(false, HereStatus.SUCCESS_CERTIFICATE_OWN)).thenReturn(expected);
//
//        ResponseSuccessDto<?> result = certificateService.saveCertificate(tokenId, lectureId, userId, cid);
//
//        assertThat(result.getData()).isEqualTo(false);
//    }
}
