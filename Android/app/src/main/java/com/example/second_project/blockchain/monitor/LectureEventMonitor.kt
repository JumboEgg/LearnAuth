package com.example.second_project.blockchain.monitor

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
import org.web3j.abi.datatypes.generated.Uint16
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "LectureSystem"

data class ParsedEvent(
    val title: String,
    val amount: BigInteger,
    val date: String,
    val timestamp: Long      // 밀리초 단위의 실제 발생 시각
)

class LectureEventMonitor(
    contractAddress: String,
    val web3j: Web3j,
    transactionManager: org.web3j.tx.TransactionManager,
    gasProvider: ContractGasProvider
) : Contract("", contractAddress, web3j, transactionManager, gasProvider) {

    // 블록 해시별 타임스탬프 캐시
    private val blockTimeCache = mutableMapOf<String, Long>()

    companion object {
        fun load(
            contractAddress: String,
            web3j: Web3j,
            txManager: org.web3j.tx.TransactionManager,
            gasProvider: ContractGasProvider
        ): LectureEventMonitor {
            return LectureEventMonitor(contractAddress, web3j, txManager, gasProvider)
        }

        fun load(
            contractAddress: String,
            web3j: Web3j,
            credentials: Credentials,
            gasProvider: ContractGasProvider
        ): LectureEventMonitor {
            return LectureEventMonitor(
                contractAddress,
                web3j,
                org.web3j.tx.ClientTransactionManager(web3j, credentials.address),
                gasProvider
            )
        }
    }

    val events = mapOf(
        "TokenDeposited" to Event(
            "TokenDeposited",
            listOf(
                TypeReference.create(Uint256::class.java, true),  // 사용자 ID
                TypeReference.create(Uint256::class.java),          // 입금 금액
                TypeReference.create(Utf8String::class.java)        // 활동 유형
            )
        ),
        "TokenWithdrawn" to Event(
            "TokenWithdrawn",
            listOf(
                TypeReference.create(Uint256::class.java, true),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Utf8String::class.java)
            )
        ),
        "LecturePurchased" to Event(
            "LecturePurchased",
            listOf(
                TypeReference.create(Uint256::class.java, true),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Utf8String::class.java)
            )
        ),
        // 정산 이벤트 추가
        "LectureSettled" to Event(
            "LectureSettled",
            listOf(
                TypeReference.create(Uint16::class.java, true),    // 참가자 ID (indexed)
                TypeReference.create(Uint16::class.java, true),    // 강의 ID (indexed)
                TypeReference.create(Uint256::class.java),         // 정산 금액
                TypeReference.create(Utf8String::class.java)       // 강의 제목
            )
        )
    )

    // 입금 이벤트 모니터링
    fun tokenDepositedEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = events["TokenDeposited"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress)
                    .addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val transactionEvent = TransactionEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                activityType = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(
                TAG,
                "TokenDeposited: ${transactionEvent.amount}, UserId: ${transactionEvent.userId}"
            )
            transactionEvent
        }
    }

    // 출금 이벤트 모니터링
    fun tokenWithdrawnEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = events["TokenWithdrawn"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress)
                    .addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val transactionEvent = TransactionEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                activityType = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(
                TAG,
                "TokenWithdrawn: ${transactionEvent.amount}, UserId: ${transactionEvent.userId}"
            )
            transactionEvent
        }
    }

    // 강의 구매 이벤트 모니터링
    fun lecturePurchasedEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<LecturePurchaseEvent> {
        val event = events["LecturePurchased"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress)
                    .addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val lecturePurchaseEvent = LecturePurchaseEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                lectureTitle = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(
                TAG,
                "LecturePurchased: ${lecturePurchaseEvent.amount}, UserId: ${lecturePurchaseEvent.userId}, Title: ${lecturePurchaseEvent.lectureTitle}"
            )
            lecturePurchaseEvent
        }
    }

    // 과거 이벤트 로그를 조회하여 ParsedEvent 목록을 반환 (블록 타임스탬프 캐시 적용)
    suspend fun getEventLogs(
        eventName: String,
        userId: BigInteger,
        web3j: Web3j,
        contractAddress: String,
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): List<ParsedEvent> = withContext(Dispatchers.IO) {
        val event = events[eventName] ?: return@withContext emptyList()
        val filter = EthFilter(fromBlock, toBlock, contractAddress).apply {
            addSingleTopic(EventEncoder.encode(event))
            // userId 필터 (32자리 16진수)
            addOptionalTopics("0x" + userId.toString(16).padStart(64, '0'))
        }
        val logs = web3j.ethGetLogs(filter)
            .send().logs.mapNotNull { it as? org.web3j.protocol.core.methods.response.Log }
        logs.mapNotNull { log ->
            try {
                val eventValues = Contract.staticExtractEventParameters(event, log)
                val rawAmount = eventValues.nonIndexedValues[0].value as BigInteger
                val amount = rawAmount.divide(BigInteger.TEN.pow(18))
                val title: String = when (eventName) {
                    "TokenDeposited" -> "토큰 충전"
                    "TokenWithdrawn" -> "토큰 출금"
                    "LecturePurchased" -> eventValues.nonIndexedValues[1].value as String
                    else -> ""
                }
                // 블록 타임스탬프 캐시 사용
                val blockHash = log.blockHash
                val timestamp = blockTimeCache.getOrPut(blockHash) {
                    val block = web3j.ethGetBlockByHash(blockHash, false).send().block
                    block.timestamp.toLong() * 1000  // 밀리초 단위로 변환
                }
                val date = SimpleDateFormat("yyyy / MM / dd", Locale.getDefault())
                    .format(Date(timestamp))
                ParsedEvent(title, amount, date, timestamp)
            } catch (e: Exception) {
                Log.e(TAG, "이벤트 로그 파싱 실패: ${e.message}")
                null
            }
        }
    }

    fun lectureSettledEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<SettlementEvent> {
        val event = events["LectureSettled"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress)
                    .addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val settlementEvent = SettlementEvent(
                participantId = eventValues.indexedValues[0].value as BigInteger,
                lectureId = eventValues.indexedValues[1].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                lectureTitle = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(
                TAG,
                "LectureSettled: 참가자=${settlementEvent.participantId}, 강의=${settlementEvent.lectureTitle}, 금액=${settlementEvent.amount}"
            )
            settlementEvent
        }
    }

    // LectureEventMonitor 클래스에 정산 이벤트 로그 조회 메서드도 추가
    suspend fun getSettlementEventLogs(
        userId: BigInteger,
        web3j: Web3j,
        contractAddress: String,
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): List<ParsedEvent> = withContext(Dispatchers.IO) {
        val event = events["LectureSettled"] ?: return@withContext emptyList()
        val filter = EthFilter(fromBlock, toBlock, contractAddress).apply {
            addSingleTopic(EventEncoder.encode(event))
            // userId 필터 (32자리 16진수)
            addOptionalTopics("0x" + userId.toString(16).padStart(64, '0'))
        }
        val logs = web3j.ethGetLogs(filter)
            .send().logs.mapNotNull { it as? org.web3j.protocol.core.methods.response.Log }
        logs.mapNotNull { log ->
            try {
                val eventValues = Contract.staticExtractEventParameters(event, log)
                val rawAmount = eventValues.nonIndexedValues[0].value as BigInteger
                val amount = rawAmount.divide(BigInteger.TEN.pow(18))
                val title = eventValues.nonIndexedValues[1].value as String

                // 블록 타임스탬프 캐시 사용
                val blockHash = log.blockHash
                val timestamp = blockTimeCache.getOrPut(blockHash) {
                    val block = web3j.ethGetBlockByHash(blockHash, false).send().block
                    block.timestamp.toLong() * 1000  // 밀리초 단위로 변환
                }
                val date = SimpleDateFormat("yyyy / MM / dd", Locale.getDefault())
                    .format(Date(timestamp))

                ParsedEvent("강의 수입: $title", amount, date, timestamp)
            } catch (e: Exception) {
                Log.e(TAG, "정산 이벤트 로그 파싱 실패: ${e.message}")
                null
            }
        }
    }
}

data class SettlementEvent(
    val participantId: BigInteger,  // 정산금을 받는 사람의 ID
    val lectureId: BigInteger,      // 강의 ID
    val amount: BigInteger,         // 정산 금액
    val lectureTitle: String        // 강의 제목
)