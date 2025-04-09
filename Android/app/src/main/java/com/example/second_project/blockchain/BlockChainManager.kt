package com.example.second_project.blockchain

import android.util.Log
import com.example.second_project.blockchain.monitor.LectureEventMonitor
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.EventEncoder
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log as Web3Log
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Contract
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.io.File
import java.math.BigInteger

private const val TAG = "BlockChainManager_야옹"

// ==========================
// 이벤트 DTO 예시 (원하는 대로 커스텀 가능)
// ==========================
data class TransactionEvent(
    val userId: BigInteger,
    val amount: BigInteger,
    val activityType: String // 예: "Deposit", "Withdraw"
)

data class LecturePurchaseEvent(
    val userId: BigInteger,
    val amount: BigInteger,
    val lectureTitle: String
)

/**
 * 지갑, 컨트랙트 로드, 이벤트를 수동(replay)으로 관리
 */
class BlockChainManager(
    private val walletPassword: String,
    private val walletFile: File
) {

    val web3j: Web3j = Web3j.build(HttpService("https://rpc-amoy.polygon.technology/"))
    val credentials: Credentials = WalletUtils.loadCredentials(walletPassword, walletFile)

    // Amoy 테스트넷 체인ID
    private val chainId = 80002L
    private val txManager: TransactionManager = RawTransactionManager(web3j, credentials, chainId)
    private val gasProvider: ContractGasProvider = HighGasProvider()

    // 컨트랙트 인스턴스들
    val lectureEventMonitor: LectureEventMonitor
    val catToken: CATToken
    val forwarder: LectureForwarder

    init {
        // 실제 배포 주소 (예시는 가짜)
        val addresses = mapOf(
            "LectureForwarder" to "0x6C8dB305b62f8b2C25d89EB8cBcD34f04A1b18Da",
            "CATToken"         to "0xBbA194679E8C86c722Ea5423e26f47D18d0f7633",
            "LectureSystem"    to "0x967a5f3B77949DE8b7ebf7392fF2B63dc1a5add0"
        )

        lectureEventMonitor = LectureEventMonitor.load(
            addresses["LectureSystem"]!!,
            web3j,
            txManager,
            gasProvider
        )
        catToken = CATToken.load(
            addresses["CATToken"]!!,
            web3j,
            txManager,
            gasProvider
        )
        forwarder = LectureForwarder.load(
            addresses["LectureForwarder"]!!,
            web3j,
            txManager,
            gasProvider
        )
    }

    /**
     * (A) "TokenDeposited" 과거+미래 로그 Flowable (수동 방식)
     *  - fromBlock ~ toBlock 의 과거 로그는 ethGetLogs()로
     *  - 앞으로 새로 발생하는 로그는 ethLogFlowable()로
     */
    fun subscribePastAndFutureTokenDepositedManual(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = lectureEventMonitor.events["TokenDeposited"] ?: return Flowable.empty()

        // 1) EthFilter 설정
        val filter = EthFilter(fromBlock, toBlock, lectureEventMonitor.contractAddress)
            .addSingleTopic(EventEncoder.encode(event))
        // 만약 userId별로 필터링하고 싶으면 addOptionalTopics("0x123abc...") 등을 추가

        // 2) Flowable 수동 생성
        return Flowable.create({ emitter: FlowableEmitter<TransactionEvent> ->
            try {
                // ------------------------------------------------
                // (a) 과거 로그: ethGetLogs() 로 한번에 조회
                // ------------------------------------------------
                val logsResponse = web3j.ethGetLogs(filter).send()
                val pastLogs = logsResponse.logs.mapNotNull { it as? Web3Log }

                // 과거 로그 파싱 후 emitter로 전달
                for (log in pastLogs) {
                    val ev = Contract.staticExtractEventParameters(event, log)
                    val userId = ev.indexedValues[0].value as BigInteger
                    val amount = ev.nonIndexedValues[0].value as BigInteger
                    val activityType = ev.nonIndexedValues[1].value as String

                    emitter.onNext(TransactionEvent(userId, amount, activityType))
                }

                // ------------------------------------------------
                // (b) 미래(실시간) 로그: ethLogFlowable()로 구독
                // ------------------------------------------------
                val futureFlowable = web3j.ethLogFlowable(filter)

                // 실시간 구독
                futureFlowable.subscribe(
                    { newLog ->
                        val ev = Contract.staticExtractEventParameters(event, newLog)
                        val userId = ev.indexedValues[0].value as BigInteger
                        val amount = ev.nonIndexedValues[0].value as BigInteger
                        val activityType = ev.nonIndexedValues[1].value as String

                        emitter.onNext(TransactionEvent(userId, amount, activityType))
                    },
                    { error ->
                        emitter.onError(error)
                    }
                )

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER)
        // 필요하다면 .subscribeOn(Schedulers.io()) / .observeOn(AndroidSchedulers.mainThread()) 등
    }


    /**
     * (B) "TokenWithdrawn" 과거+미래 로그 Flowable (수동 방식)
     */
    fun subscribePastAndFutureTokenWithdrawnManual(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = lectureEventMonitor.events["TokenWithdrawn"] ?: return Flowable.empty()

        val filter = EthFilter(fromBlock, toBlock, lectureEventMonitor.contractAddress)
            .addSingleTopic(EventEncoder.encode(event))

        return Flowable.create({ emitter: FlowableEmitter<TransactionEvent> ->
            try {
                // 1) 과거 로그
                val logsResponse = web3j.ethGetLogs(filter).send()
                val pastLogs = logsResponse.logs.mapNotNull { it as? Web3Log }
                for (log in pastLogs) {
                    val ev = Contract.staticExtractEventParameters(event, log)
                    val userId = ev.indexedValues[0].value as BigInteger
                    val amount = ev.nonIndexedValues[0].value as BigInteger
                    val activityType = ev.nonIndexedValues[1].value as String

                    emitter.onNext(TransactionEvent(userId, amount, activityType))
                }

                // 2) 실시간 로그
                val futureFlowable = web3j.ethLogFlowable(filter)
                futureFlowable.subscribe(
                    { newLog ->
                        val ev = Contract.staticExtractEventParameters(event, newLog)
                        val userId = ev.indexedValues[0].value as BigInteger
                        val amount = ev.nonIndexedValues[0].value as BigInteger
                        val activityType = ev.nonIndexedValues[1].value as String

                        emitter.onNext(TransactionEvent(userId, amount, activityType))
                    },
                    { error -> emitter.onError(error) }
                )

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER)
    }


    /**
     * (C) "LecturePurchased" 과거+미래 로그 Flowable (수동 방식)
     */
    fun subscribePastAndFutureLecturePurchasedManual(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<LecturePurchaseEvent> {
        val event = lectureEventMonitor.events["LecturePurchased"] ?: return Flowable.empty()

        val filter = EthFilter(fromBlock, toBlock, lectureEventMonitor.contractAddress)
            .addSingleTopic(EventEncoder.encode(event))

        return Flowable.create({ emitter: FlowableEmitter<LecturePurchaseEvent> ->
            try {
                // 1) 과거 로그
                val logsResponse = web3j.ethGetLogs(filter).send()
                val pastLogs = logsResponse.logs.mapNotNull { it as? Web3Log }
                for (log in pastLogs) {
                    val ev = Contract.staticExtractEventParameters(event, log)
                    val userId = ev.indexedValues[0].value as BigInteger
                    val amount = ev.nonIndexedValues[0].value as BigInteger
                    val title = ev.nonIndexedValues[1].value as String

                    emitter.onNext(LecturePurchaseEvent(userId, amount, title))
                }

                // 2) 미래(실시간)
                val futureFlowable = web3j.ethLogFlowable(filter)
                futureFlowable.subscribe(
                    { newLog ->
                        val ev = Contract.staticExtractEventParameters(event, newLog)
                        val userId = ev.indexedValues[0].value as BigInteger
                        val amount = ev.nonIndexedValues[0].value as BigInteger
                        val title = ev.nonIndexedValues[1].value as String

                        emitter.onNext(LecturePurchaseEvent(userId, amount, title))
                    },
                    { error -> emitter.onError(error) }
                )

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER)
    }


    // ==================================
    // 아래는 기존 코드 (필요에 따라 유지)
    // ==================================

    suspend fun getTransactionHistory(userId: BigInteger) {
        withContext(Dispatchers.IO) {
            // 이 함수는 earliest~latest 전체 구독이라
            // 퍼블릭 노드에서 filter not found가 빈번히 발생 가능
            // => 필요 없다면 제거 권장
            lectureEventMonitor.tokenDepositedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(
                { event ->
                    if (event.userId == userId) {
                        Log.d(TAG, "💰 Deposit: ${event.amount}, ${event.activityType}")
                    }
                },
                { error -> Log.e(TAG, "Error fetching deposits", error) }
            )
            // 이하 생략...
        }
    }

    fun getMyCatTokenBalance(): BigInteger {
        val address = credentials.address
        return catToken.balanceOf(address).sendAsync().get()
    }

    fun getMyWalletAddress(): String {
        return credentials.address
    }
}
