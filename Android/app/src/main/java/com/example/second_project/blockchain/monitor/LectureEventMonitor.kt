package com.example.second_project.blockchain.monitor;

import io.reactivex.rxjava3.core.Flowable
import android.util.Log
import org.web3j.abi.EventEncoder
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import org.web3j.abi.TypeReference

private const val TAG = "LectureSystem_야옹"

data class TransactionEvent(
    val userId: BigInteger, //거래자 ID
    val amount: BigInteger, // 거래 금액 (토큰단위)
    val activityType: String //거래 유형 (입/출금)
)

data class LecturePurchaseEvent(
    val userId: BigInteger, // 강의구매자
    val amount: BigInteger, // 강의구매비용
    val lectureTitle: String // 구매한 강의 제목
)

class LectureSystem(
    contractAddress: String,
    web3j: Web3j,
    transactionManager: org.web3j.tx.TransactionManager,
    gasProvider: ContractGasProvider
) : Contract("", contractAddress, web3j, transactionManager, gasProvider) {

    companion object {

        // ✅ 일반 트랜잭션용 (RawTransactionManager 기반 등)
        fun load(
            contractAddress: String,
            web3j: Web3j,
            txManager: org.web3j.tx.TransactionManager,
            gasProvider: ContractGasProvider
        ): LectureSystem {
            return LectureSystem(contractAddress, web3j, txManager, gasProvider)
        }

        // ✅ MetaTx 서명용 - 프론트에서는 Credentials 기반으로 contract 객체 생성 가능
        fun load(
            contractAddress: String,
            web3j: Web3j,
            credentials: Credentials,
            gasProvider: ContractGasProvider
        ): LectureSystem {
            return LectureSystem(
                contractAddress,
                web3j,
                org.web3j.tx.ClientTransactionManager(web3j, credentials.address), // read-only
                gasProvider
            )
        }
    }

    // 스마트 컨트랙트에서 발생하는 이벤트 목록 정의
    private val events = mapOf(
        "TokenDeposited" to Event(
            "TokenDeposited",
            listOf(
                TypeReference.create(Uint256::class.java, true), //사용자 ID
                TypeReference.create(Uint256::class.java), //입금 금액
                TypeReference.create(Utf8String::class.java) // 활동 유형
            )
        ),
        "TokenWithdrawn" to Event(
            "TokenWithdrawn",
            listOf(
                TypeReference.create(Uint256::class.java, true), // ID
                TypeReference.create(Uint256::class.java), //출금 금액
                TypeReference.create(Utf8String::class.java)
            )
        ),
        "LecturePurchased" to Event(
            "LecturePurchased",
            listOf(
                TypeReference.create(Uint256::class.java, true),
                TypeReference.create(Uint256::class.java), // 구매 금액
                TypeReference.create(Utf8String::class.java)
            )
        )
    )

    // 입금 이벤트 모니터링하는 함수
    fun tokenDepositedEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = events["TokenDeposited"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress).addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val transactionEvent = TransactionEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                activityType = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(TAG, "tokenDepositedEventFlowable: ${transactionEvent.amount}, UserId: ${transactionEvent.userId}, Type: ${transactionEvent.activityType}")
            transactionEvent
        }
    }

    // 출금 이벤트 모니터링 함수
    fun tokenWithdrawnEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = events["TokenWithdrawn"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress).addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val transactionEvent = TransactionEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                activityType = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(TAG, "TokenWithdrawn: ${transactionEvent.amount}, UserId: ${transactionEvent.userId}, Type: ${transactionEvent.activityType}")
            transactionEvent
        }
    }

    // 강의 구매 이벤트 모니터링 함수
    fun lecturePurchasedEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<LecturePurchaseEvent> {
        val event = events["LecturePurchased"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress).addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val lecturePurchaseEvent = LecturePurchaseEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                lectureTitle = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(TAG, "LecturePurchased: ${lecturePurchaseEvent.amount}, UserId: ${lecturePurchaseEvent.userId}, Title: ${lecturePurchaseEvent.lectureTitle}")
            lecturePurchaseEvent
        }
    }
}