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
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "BlockChainManager_야옹"

// ==========================
// 이벤트 DTO 예시 (원하는 대로 커스텀 가능)
// ==========================
data class TransactionEvent(
    val userId: BigInteger,
    val amount: BigInteger,
    val activityType: String
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


    private var cachedBalance: BigInteger? = null
    private var lastBalanceCheckTime: Long = 0
    private val BALANCE_CACHE_DURATION = 3000L  // 3초 캐싱

    private var initializationTime: Long = 0

    init {
        // 실제 배포 주소 (예시는 가짜)
        val addresses = mapOf(
            "LectureForwarder" to "0x6C8dB305b62f8b2C25d89EB8cBcD34f04A1b18Da",
            "CATToken" to "0xBbA194679E8C86c722Ea5423e26f47D18d0f7633",
            "LectureSystem" to "0x967a5f3B77949DE8b7ebf7392fF2B63dc1a5add0"
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
        val currentTime = System.currentTimeMillis()

        // 캐시 확인: 유효 기간(3초) 내에 조회된 값이 있으면 재사용
        if (cachedBalance != null && currentTime - lastBalanceCheckTime < BALANCE_CACHE_DURATION) {
            Log.d(TAG, "💰 캐시된 잔액 반환: $cachedBalance")
            return cachedBalance!!
        }

        // 캐시가 없거나 만료된 경우 블록체인에서 새로 조회
        try {
            val address = credentials.address
            val startTime = System.currentTimeMillis()

            // 동기 호출 대신 비동기 호출 후 결과 대기 (타임아웃 적용)
            val balanceFuture = catToken.balanceOf(address).sendAsync()

            // 최대 5초 대기 (원래는 무한정 대기할 수 있음)
            val balance = balanceFuture.get()

            val elapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "💰 잔액 조회 완료: $balance (소요시간: ${elapsed}ms)")

            // 결과 캐싱
            cachedBalance = balance
            lastBalanceCheckTime = currentTime

            return balance
        } catch (e: Exception) {
            Log.e(TAG, "❌ 잔액 조회 실패: ${e.message}")

            // 오류 발생 시 캐시된 값이 있으면 그것이라도 반환
            cachedBalance?.let {
                Log.d(TAG, "💰 오류 발생, 캐시된 값으로 대체: $it")
                return it
            }

            // 아무것도 없으면 예외 발생
            throw e
        }
    }

    suspend fun getMyCatTokenBalanceAsync(): BigInteger = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()

        // 캐시 확인
        if (cachedBalance != null && currentTime - lastBalanceCheckTime < BALANCE_CACHE_DURATION) {
            Log.d(TAG, "💰 캐시된 잔액 반환 (async): $cachedBalance")
            return@withContext cachedBalance!!
        }

        // 블록체인에서 새로 조회 (코루틴 방식)
        try {
            val address = credentials.address
            val startTime = System.currentTimeMillis()

            // suspendCoroutine을 사용하여 코루틴으로 변환
            val balance = suspendCoroutine<BigInteger> { continuation ->
                val future = catToken.balanceOf(address).sendAsync()

                future.whenComplete { result, error ->
                    if (error != null) {
                        continuation.resumeWithException(error)
                    } else {
                        continuation.resume(result)
                    }
                }
            }

            val elapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "💰 잔액 조회 완료 (async): $balance (소요시간: ${elapsed}ms)")

            // 결과 캐싱
            cachedBalance = balance
            lastBalanceCheckTime = currentTime

            return@withContext balance
        } catch (e: Exception) {
            Log.e(TAG, "❌ 잔액 조회 실패 (async): ${e.message}")

            // 오류 발생 시 캐시된 값이 있으면 반환
            cachedBalance?.let {
                Log.d(TAG, "💰 오류 발생, 캐시된 값으로 대체 (async): $it")
                return@withContext it
            }

            throw e
        }
    }

    fun preloadBalanceInBackground(): CompletableFuture<BigInteger> {
        val future = CompletableFuture<BigInteger>()

        Thread {
            try {
                val address = credentials.address
                val balance = catToken.balanceOf(address).send()

                // 결과 캐싱
                cachedBalance = balance
                lastBalanceCheckTime = System.currentTimeMillis()

                Log.d(TAG, "💰 잔액 백그라운드 로드 완료: $balance")
                future.complete(balance)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 잔액 백그라운드 로드 실패: ${e.message}")
                future.completeExceptionally(e)
            }
        }.start()

        return future
    }

    /**
     * 잔액 강제 갱신 메소드
     * - 캐시를 무시하고 항상 새로운 값을 조회
     * - 충전 등 잔액 변경 후 즉시 반영이 필요할 때 사용
     */
    fun forceRefreshBalance(): BigInteger {
        try {
            // 캐시 초기화
            cachedBalance = null
            lastBalanceCheckTime = 0

            val address = credentials.address
            val startTime = System.currentTimeMillis()

            // 동기 호출 사용 (중요한 업데이트이므로)
            val balance = catToken.balanceOf(address).send()

            val elapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "💰 잔액 강제 갱신 완료: $balance (소요시간: ${elapsed}ms)")

            // 결과 캐싱
            cachedBalance = balance
            lastBalanceCheckTime = System.currentTimeMillis()

            return balance
        } catch (e: Exception) {
            Log.e(TAG, "❌ 잔액 강제 갱신 실패: ${e.message}")
            throw e
        }
    }
    private var cachedAddress: String? = null

    fun getMyWalletAddress(): String {
        // 주소는 변경되지 않으므로 한 번만 계산하면 됨
        if (cachedAddress == null) {
            cachedAddress = credentials.address
        }
        return cachedAddress!!
    }
}
