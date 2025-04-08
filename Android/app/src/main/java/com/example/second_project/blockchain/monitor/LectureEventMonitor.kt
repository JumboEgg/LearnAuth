package com.example.second_project.blockchain.monitor;

import io.reactivex.rxjava3.core.Flowable
import android.util.Log
import com.example.second_project.blockchain.LecturePurchaseEvent
import com.example.second_project.blockchain.TransactionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "LectureSystem_야옹"

data class ParsedEvent(
    val title: String,
    val amount: BigInteger,
    val date: String
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
    val events = mapOf(
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
            Log.d(TAG, "tokenWithdrawnEventFlowable: ${eventValues.indexedValues}")
            Log.d(TAG, "tokenWithdrawnEventFlowable: ${eventValues.nonIndexedValues}")
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

    suspend fun getEventLogs(
        eventName: String,
        userId: BigInteger,
        web3j: Web3j,
        contractAddress: String,
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): List<ParsedEvent> = withContext(Dispatchers.IO) {
        val event = events[eventName] ?: return@withContext emptyList()
        val filter = org.web3j.protocol.core.methods.request.EthFilter(fromBlock, toBlock, contractAddress).apply {
            addSingleTopic(EventEncoder.encode(event))
            // userId 필터 (32자리 16진수 문자열로 패딩)
            addOptionalTopics("0x" + userId.toString(16).padStart(64, '0'))
        }
        val logs = web3j.ethGetLogs(filter).send().logs
            .mapNotNull { it as? org.web3j.protocol.core.methods.response.Log }
        logs.mapNotNull { log ->
            try {
                val eventValues = staticExtractEventParameters(event, log)
                // 첫번째 non-indexed 값: 거래 금액
                val rawAmount = eventValues.nonIndexedValues[0].value as BigInteger
                val amount = rawAmount.divide(BigInteger.TEN.pow(18))
                // 이벤트 종류에 따라 title 결정
                val title: String = when (eventName) {
                    "TokenDeposited" -> "토큰 충전"
                    "TokenWithdrawn" -> "토큰 출금"
                    "LecturePurchased" -> eventValues.nonIndexedValues[1].value as String
                    else -> ""
                }
                // 로그가 포함된 블록의 타임스탬프를 가져와 날짜 형식으로 변환
                val block = web3j.ethGetBlockByHash(log.blockHash, false).send()
                val timestamp = block.block.timestamp.toLong() * 1000
                val date = SimpleDateFormat("yyyy / MM / dd", Locale.getDefault()).format(Date(timestamp))
                ParsedEvent(
                    title = title,
                    amount = amount,
                    date = date
                )
            } catch (e: Exception) {
                Log.e(TAG, "과거 로그 파싱 실패: ${e.message}")
                null
            }
        }
    }
}