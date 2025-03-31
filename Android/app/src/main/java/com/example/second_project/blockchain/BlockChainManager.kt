package com.example.second_project.blockchain

import LecturePurchaseEvent
import LectureSystem
import TransactionEvent
import android.util.Log
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

private const val TAG = "BlockChainManager_야옹"

class BlockchainManager {
    private val web3j: Web3j // 블록체인 네트워크와 연결하는 객체
    private val lectureSystem: LectureSystem // 스마트 컨트랙트 객체
    private val credentials: Credentials //사용자 지갑 정보 (개인 키)

    init {
        // Web3j 설정
        web3j = Web3j.build(HttpService("https://rpc-amoy.polygon.technology/"))

        // 배포된 스마트 컨트랙트 주소
        val lectureSystemAddress = "0x5532EDfa8C6a10e0FA62Cc8f8c221c1573D0fcbc"

        // 테스트용 지갑의 개인 키 (실제 서비스에서는 노출 금지?)
        credentials = Credentials.create("0000000000000000000000000000000000000000000000000000000000000000")

        // LectureSystem 스마트 컨트랙트 로드 (배포된 컨트랙트와 연결)
        lectureSystem = LectureSystem.load(
            lectureSystemAddress,
            web3j,
            credentials,
            DefaultGasProvider()
        )
    }

    // 특정 사용자의 블록체인 거래 내역 조회하는 함수
    suspend fun getTransactionHistory(userId: BigInteger) {
        withContext(Dispatchers.IO) {
            // 입금 기록 가져오는 Flowable (블록체인 이벤트 리스너)
            val depositFlowable: Flowable<TransactionEvent> = lectureSystem.tokenDepositedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ) ?: Flowable.empty()

            Log.d(TAG, "getTransactionHistory: 입금 이벤트!")

            depositFlowable.subscribe(
                { event: TransactionEvent ->
                    if (event.userId == userId) {
                        Log.d(TAG, "Deposit: Amount=${event.amount}, Type=${event.activityType}")
                    }
                },
                { error ->
                    Log.e(TAG, "Error fetching deposits", error)
                },
                {
                    Log.d(TAG, "Deposit events fetched.")
                }
            )

            // 출금 이벤트
            val withdrawalFlowable: Flowable<TransactionEvent> = lectureSystem.tokenWithdrawnEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ) ?: Flowable.empty()
            Log.d(TAG, "getTransactionHistory: 출금 이벤트!")

            withdrawalFlowable.subscribe(
                { event: TransactionEvent ->
                    if (event.userId == userId) {
                        Log.d(TAG, "Withdraw: Amount=${event.amount}, Type=${event.activityType}")
                    }
                },
                { error ->
                    Log.e(TAG, "Error fetching withdrawals", error)
                },
                {
                    Log.d(TAG, "Withdrawal events fetched.")
                }
            )

            // 강의 구매 이벤트
            val purchaseFlowable: Flowable<LecturePurchaseEvent> = lectureSystem.lecturePurchasedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ) ?: Flowable.empty()

            Log.d(TAG, "getTransactionHistory: 강의 구매 이벤트!")

            purchaseFlowable.subscribe(
                { event: LecturePurchaseEvent ->
                    if (event.userId == userId) {
                        Log.d(TAG, "Purchase: Amount=${event.amount}, Lecture=${event.lectureTitle}")
                    }
                },
                { error ->
                    Log.e(TAG, "Error fetching purchases", error)
                },
                {
                    Log.d(TAG, "Purchase events fetched.")
                }
            )
        }
    }
}
