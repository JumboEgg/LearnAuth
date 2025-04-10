package ssafy.d210.backend.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import org.web3j.protocol.core.RemoteFunctionCall;
//import org.web3j.protocol.core.methods.response.TransactionReceipt;
//import ssafy.d210.backend.blockchain.AccountManager;
//import ssafy.d210.backend.blockchain.ContractServiceFactory;
//import ssafy.d210.backend.blockchain.RelayerAccount;
//import ssafy.d210.backend.contracts.CATToken;
//import ssafy.d210.backend.contracts.LectureSystem;
//import ssafy.d210.backend.dto.common.ResponseSuccessDto;
//
//import java.math.BigInteger;
//import java.util.concurrent.CompletableFuture;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
//
//    @Mock
//    private AccountManager accountManager;
//
//    @Mock
//    private ContractServiceFactory contractServiceFactory;
//
//    @Mock
//    private CATToken catToken;
//
//    @Mock
//    private LectureSystem lectureSystem;
//
//    @Mock
//    private RelayerAccount relayerAccount;
//
//    @InjectMocks
//    private PaymentServiceImpl paymentService;
//
//    @Test
//    @DisplayName("토큰 증가 성공")
//    void increaseTokenSuccess() throws Exception {
//        long userId = 1L;
//        BigInteger quantity = new BigInteger("1000000000000000000");
//
//        TransactionReceipt approvalReceipt = new TransactionReceipt();
//        approvalReceipt.setTransactionHash("0x123");
//        TransactionReceipt depositReceipt = new TransactionReceipt();
//        depositReceipt.setTransactionHash("0x456");
//
//        when(accountManager.acquireAccount()).thenReturn(relayerAccount);
//        when(relayerAccount.getAddress()).thenReturn("0xRelayer");
//        when(contractServiceFactory.createCATToken(any())).thenReturn(catToken);
//        when(contractServiceFactory.createLectureSystem(any())).thenReturn(lectureSystem);
//
//        when(catToken.approve(any(), any())).thenReturn(mock(RemoteFunctionCall.class));
//        when(catToken.approve(any(), any()).send()).thenReturn(approvalReceipt);
//
//        CompletableFuture<TransactionReceipt> future = CompletableFuture.completedFuture(depositReceipt);
//        when(lectureSystem.depositToken(any(), any())).thenReturn(mock(RemoteFunctionCall.class));
//        when(lectureSystem.depositToken(any(), any()).sendAsync()).thenReturn(future);
//
//        ResponseSuccessDto<Boolean> result = paymentService.increaseToken(userId, quantity);
//
//        assertThat(result.getData()).isTrue();
//        verify(accountManager).releaseAccount(relayerAccount);
//    }
//
//    @Test
//    @DisplayName("토큰 증가 실패")
//    void increaseTokenFailure() throws Exception {
//        long userId = 1L;
//        BigInteger quantity = new BigInteger("1000000000000000000");
//
//        when(accountManager.acquireAccount()).thenThrow(new RuntimeException("Account error"));
//
//        ResponseSuccessDto<Boolean> result = paymentService.increaseToken(userId, quantity);
//
//        assertThat(result.getData()).isFalse();
//    }
}
