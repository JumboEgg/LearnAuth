package ssafy.d210.backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.payment.TokenRequest;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.service.MetaTransactionService;
import ssafy.d210.backend.service.PaymentService;
import ssafy.d210.backend.util.ResponseUtil;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "PaymentController", description = "토큰 구매 및 차감")
public class PaymentController {

    private final ResponseUtil responseUtil;
    private final PaymentService paymentService;
    private final MetaTransactionService metaTransactionService;

    // 토큰 추가
    @PatchMapping("/withdrawal")
    @Operation(summary = "토큰 차감", description = "출금 시 {userId}에서 {quantity}만큼 토큰을 차감한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 차감 완료")
    })
    public ResponseEntity<ResponseSuccessDto<Boolean>> withdrawToken(
            @RequestBody SignedRequest signedRequest
            ) {
        ResponseSuccessDto<Boolean> res = responseUtil.successResponse(metaTransactionService.executeMetaTransaction(signedRequest), HereStatus.SUCCESS_LECTURE);
        return ResponseEntity.ok(res);
    }

    // 토큰 차감
    @PatchMapping("/deposit")
    @Operation(summary = "토큰 추가", description = "결제 시 {userId}의 지갑에 {quantity}만틈 토큰을 지급힌다.")
    public ResponseEntity<ResponseSuccessDto<Boolean>> depositToken(
            @RequestBody TokenRequest tokenRequest
    ) {
        return ResponseEntity.ok(paymentService.increaseToken(tokenRequest.getUserId(), tokenRequest.getQuantity()));
    }
}
