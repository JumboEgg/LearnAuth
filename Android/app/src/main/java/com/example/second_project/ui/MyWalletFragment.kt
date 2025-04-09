package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.adapter.TransactionAdapter
import com.example.second_project.blockchain.BlockChainManager
import com.example.second_project.data.TransactionCache
import com.example.second_project.data.TransactionItem
import com.example.second_project.databinding.FragmentMywalletBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyWalletFragment : Fragment() {
    private var _binding: FragmentMywalletBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private val TAG = "MyWalletFragment"
    private var blockChainManager: BlockChainManager? = null
    private val userId: BigInteger
        get() = BigInteger.valueOf(UserSession.userId.toLong())
    private var isDataLoaded = false

    // 최적화: 마지막 데이터 새로고침 시각을 저장
    private var lastRefreshTime = 0L

    // 최적화: 데이터 새로고침 간격 (밀리초) - 2분으로 설정
    private val REFRESH_INTERVAL = 2 * 60 * 1000L
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMywalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "userId: $userId (decimal), 0x${userId.toString(16)} (hex)")

        // 1. UI 초기 설정 (즉시 수행)
        setupUI()

        // 2. 기본 UI 상태 설정 (로딩 표시)
        showLoading(true)

        // 3. 캐시된 데이터가 있으면 즉시 표시
        if (UserSession.lastKnownBalance != null) {
            updateBalanceUI(UserSession.lastKnownBalance!!)
        } else {
            binding.moneyCount.text = "로딩 중..."
        }

        // 4. 캐시된 거래 내역 즉시 표시
        if (!TransactionCache.isEmpty()) {
            showTransactionData(TransactionCache.getRecentTransactions(TransactionCache.size()))
        }

        // 5. 백그라운드에서 데이터 로드 (화면 표시 지연 없음)
        CoroutineScope(Dispatchers.Main).launch {
            // 약간의 지연을 줘서 UI가 먼저 그려지게 함
            delay(100)
            // 이제 백그라운드에서 데이터 새로고침
            refreshDataInBackground()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 로딩 중일 때는 아무 작업도 하지 않음 (뒤로가기 무시)
                if (isLoading) {
                    Log.d(TAG, "로딩 중에 뒤로가기 버튼이 눌렸지만 무시됨")
                } else {
                    // 로딩 중이 아니면 정상적으로 뒤로가기 동작 수행
                    this.remove() // 현재 콜백 제거
                    requireActivity().onBackPressed() // 뒤로가기 동작 수행
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showTransactionData(transactions: List<TransactionItem>) {
        if (transactions.isEmpty()) {
            showDefaultTransactions()
        } else {
            transactionAdapter = TransactionAdapter(transactions)
            binding.transactionList.adapter = transactionAdapter
            binding.transactionList.visibility = View.VISIBLE
        }
        showLoading(false)
        isDataLoaded = true
    }

    // 새로운 함수: 백그라운드에서 데이터 새로고침

    private fun setupUI() {
        // RecyclerView와 Adapter 설정
        transactionAdapter = TransactionAdapter(emptyList())
        binding.transactionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }

        blockChainManager = UserSession.getBlockchainManagerIfAvailable(requireContext())
        binding.userName.text = UserSession.name ?: "사용자"

        // 버튼 리스너 설정
        binding.chargeBtn.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, ChargeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // 최적화: 캐시된 데이터 표시 후 필요한 경우에만 새로고침
    private fun displayCachedDataAndRefreshIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val needsRefresh = lastRefreshTime == 0L || currentTime - lastRefreshTime > REFRESH_INTERVAL

        // 1. 캐시된 잔액 즉시 표시
        UserSession.lastKnownBalance?.let {
            updateBalanceUI(it)
        }

        // 2. 캐시된 거래 내역 즉시 표시 (향상된 TransactionCache 사용)
        if (!TransactionCache.isEmpty()) {
            transactionAdapter =
                TransactionAdapter(TransactionCache.getRecentTransactions(TransactionCache.size()))
            binding.transactionList.adapter = transactionAdapter
            binding.transactionList.visibility = View.VISIBLE
            binding.loadingSpinner.visibility = View.GONE
            binding.blockTouchOverlay.visibility = View.GONE
            binding.chargeBtn.isEnabled = true
            isDataLoaded = true
        } else {
            showLoading(true)
        }

        // 3. 새로고침이 필요하거나 캐시가 비어있으면 백그라운드에서 데이터 로드
        if (needsRefresh || !TransactionCache.isFresh() || TransactionCache.isEmpty()) {
            loadBlockchainData(needsRefresh)
            lastRefreshTime = currentTime
        } else {
            Log.d(TAG, "캐시된 데이터 사용 중 (마지막 새로고침: ${Date(lastRefreshTime)})")
        }
    }

    private fun loadBlockchainData(forceRefresh: Boolean = false) {
        if (!forceRefresh && !TransactionCache.isEmpty()) {
            Log.d(TAG, "이미 캐시된 데이터가 있어 로드를 건너뜁니다")
            showLoading(false)
            return
        }

        if (blockChainManager == null) {
            Log.e(TAG, "BlockChainManager 초기화 실패")
            showDefaultData()
            showLoading(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 최신 잔액 조회 후 갱신 (캐시된 데이터가 이미 표시되고 있음)
                val actualBalance = blockChainManager!!.getMyCatTokenBalance()
                UserSession.lastKnownBalance = actualBalance

                // UI 업데이트는 메인 스레드에서 안전하게 처리
                withContext(Dispatchers.Main) {
                    // Fragment가 아직 유효한지 확인 후 UI 갱신
                    if (isAdded && _binding != null) {
                        updateBalanceUI(actualBalance)
                    }
                }

                // 2. 이전 거래 내역 로드
                loadHistoricalEvents()

                // 3. 실시간 이벤트 구독 (이미 이전에 설정되었을 수 있으므로 필요한 경우에만)
                if (forceRefresh) {
                    setupTransactionEvents()
                }

                // 4. 일정 시간 동안 데이터가 없으면 기본 데이터 표시
                kotlinx.coroutines.delay(3000) // 최적화: 5초에서 3초로 단축

                // UI 업데이트는 메인 스레드에서 안전하게 처리
                withContext(Dispatchers.Main) {
                    // Fragment가 아직 유효한지 확인 후 UI 갱신
                    if (isAdded && _binding != null) {
                        if (TransactionCache.isEmpty() && !isDataLoaded) {
                            showDefaultTransactions()
                        }
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "블록체인 데이터 로드 오류: ${e.message}")

                // UI 업데이트는 메인 스레드에서 안전하게 처리
                withContext(Dispatchers.Main) {
                    // Fragment가 아직 유효한지 확인 후 UI 갱신
                    if (isAdded && _binding != null) {
                        if (TransactionCache.isEmpty()) {
                            showDefaultData()
                        }
                        showLoading(false)
                    }
                }
            }
        }
    }

    // loadHistoricalEvents 메서드 수정
    private suspend fun loadHistoricalEvents() {
        val bcm = blockChainManager ?: return

        // 기존 캐시 데이터가 있다면 즉시 업데이트
        if (!TransactionCache.isEmpty()) {
            withContext(Dispatchers.Main) {
                // Fragment가 아직 유효한지 확인 후 UI 갱신
                if (isAdded && _binding != null) {
                    transactionAdapter =
                        TransactionAdapter(TransactionCache.getRecentTransactions(TransactionCache.size()))
                    binding.transactionList.adapter = transactionAdapter
                }
            }
        }

        // 최적화: 로그 조회를 위한 시작 블록 줄이기 (너무 오래된 로그는 조회하지 않음)
        val startBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(12345678L))
        val endBlock = DefaultBlockParameterName.LATEST

        try {
            // 세 이벤트 카테고리를 가져와서 변환
            val depositedEvents = bcm.lectureEventMonitor.getEventLogs(
                "TokenDeposited",
                userId,
                bcm.web3j,
                bcm.lectureEventMonitor.contractAddress,
                startBlock,
                endBlock
            )
                .map { evt ->
                    Log.d(TAG, "충전 이벤트: ${evt.title}, 타임스탬프: ${evt.timestamp}")
                    TransactionItem("토큰 충전", evt.date, evt.amount, evt.timestamp)
                }

            val withdrawnEvents = bcm.lectureEventMonitor.getEventLogs(
                "TokenWithdrawn",
                userId,
                bcm.web3j,
                bcm.lectureEventMonitor.contractAddress,
                startBlock,
                endBlock
            )
                .map { evt ->
                    Log.d(TAG, "출금 이벤트: ${evt.title}, 타임스탬프: ${evt.timestamp}")
                    TransactionItem("토큰 출금", evt.date, evt.amount, evt.timestamp)
                }

            val purchasedEvents = bcm.lectureEventMonitor.getEventLogs(
                "LecturePurchased",
                userId,
                bcm.web3j,
                bcm.lectureEventMonitor.contractAddress,
                startBlock,
                endBlock
            )
                .map { evt ->
                    Log.d(TAG, "구매 이벤트: ${evt.title}, 타임스탬프: ${evt.timestamp}")
                    TransactionItem(evt.title, evt.date, evt.amount, evt.timestamp)
                }

            // 정산 이벤트 조회
            val settlementEvents = bcm.lectureEventMonitor.getSettlementEventLogs(
                userId,
                bcm.web3j,
                bcm.lectureEventMonitor.contractAddress,
                startBlock,
                endBlock
            )
                .map { evt ->
                    Log.d(TAG, "정산 이벤트: ${evt.title}, 타임스탬프: ${evt.timestamp}")
                    TransactionItem(evt.title, evt.date, evt.amount, evt.timestamp)
                }

            // 전체 이벤트를 하나로 합침 (중복 제거는 updateTransactions에서 처리)
            val events = (depositedEvents + withdrawnEvents + purchasedEvents + settlementEvents)

            if (events.isNotEmpty()) {
                // 이벤트가 있을 경우에만 업데이트 (빈 리스트로 업데이트하지 않음)
                TransactionCache.updateTransactions(events)

                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        // UI 갱신
                        val transactions =
                            TransactionCache.getRecentTransactions(TransactionCache.size())
                        if (transactions.isNotEmpty()) {
                            transactionAdapter = TransactionAdapter(transactions)
                            binding.transactionList.adapter = transactionAdapter
                            binding.transactionList.scrollToPosition(0)
                            isDataLoaded = true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "이벤트 로그 조회 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    private val safeDisposables = mutableListOf<SafeDisposable>()

    // setupTransactionEvents 메서드 수정
    private fun setupTransactionEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val bcm = blockChainManager ?: return@launch

            // 기존 구독이 있다면 모두 해제 (중복 구독 방지)
            for (disposable in safeDisposables) {
                disposable.dispose()
            }
            safeDisposables.clear()

            Log.d(TAG, "이벤트 구독 설정 시작")

            // 이벤트 구독 설정
            setupDepositEventSubscription(bcm)
            setupWithdrawEventSubscription(bcm)
            setupPurchaseEventSubscription(bcm)
            setupSettlementEventSubscription(bcm)

            // 이벤트 구독 해제를 위한 라이프사이클 옵저버 설정
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext

                viewLifecycleOwner.lifecycle.addObserver(object :
                    androidx.lifecycle.LifecycleEventObserver {
                    override fun onStateChanged(
                        source: androidx.lifecycle.LifecycleOwner,
                        event: androidx.lifecycle.Lifecycle.Event
                    ) {
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                            // 백그라운드에서 안전하게 dispose 호출
                            CoroutineScope(Dispatchers.IO).launch {
                                for (disposable in safeDisposables) {
                                    disposable.dispose()
                                }
                                Log.d(TAG, "이벤트 구독 취소됨")
                            }
                        }
                    }
                })
            }

            Log.d(TAG, "이벤트 구독 설정 완료")
        }
    }

    private fun refreshDataInBackground() {
        val currentTime = System.currentTimeMillis()
        val needsRefresh = lastRefreshTime == 0L || currentTime - lastRefreshTime > REFRESH_INTERVAL

        // 최초 로드 또는 오래된 데이터일 경우 새로고침
        if (needsRefresh || !TransactionCache.isFresh() || TransactionCache.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 로딩 표시는 이미 UI에 있으므로 추가로 표시하지 않음
                    loadBlockchainData(needsRefresh)
                    lastRefreshTime = currentTime
                } catch (e: Exception) {
                    Log.e(TAG, "백그라운드 데이터 로드 실패: ${e.message}")
                }
            }
        } else {
            Log.d(TAG, "캐시된 데이터 사용 중 (마지막 새로고침: ${Date(lastRefreshTime)})")
            // 이미 데이터가 있으면 로딩 표시 제거
            showLoading(false)
        }
    }

    private fun setupDepositEventSubscription(bcm: BlockChainManager) {
        try {
            // 메인 스레드에서 네트워크 작업 수행하지 않도록 수정
            // 블록 번호 가져오는 부분 제거하고 LATEST만 사용
            Log.d(TAG, "충전 이벤트 구독 시작")

            val depositDisposable = bcm.lectureEventMonitor.tokenDepositedEventFlowable(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(12345678L)),
                DefaultBlockParameterName.LATEST
            )
                .subscribe({ event ->
                    Log.d(
                        TAG,
                        "충전 이벤트 감지: userId=${event.userId}, 내 userId=$userId, 금액=${event.amount}"
                    )
                    if (event.userId == userId) {
                        Log.d(TAG, "내 충전 이벤트 감지: ${event.amount}")
                        val now = System.currentTimeMillis()
                        val date = getToday(now)
                        val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                        val transactionItem = TransactionItem("토큰 충전", date, convertedAmount, now)

                        // UI 스레드에서 처리
                        CoroutineScope(Dispatchers.Main).launch {
                            if (isAdded && _binding != null) {
                                // 1. 먼저 TransactionCache에 추가
                                val added = TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "트랜잭션 추가 결과: $added")

                                // 2. 잔액도 같이 새로고침 - IO 스레드에서 수행
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val newBalance = bcm.getMyCatTokenBalance()
                                        UserSession.lastKnownBalance = newBalance

                                        // UI 업데이트는 메인 스레드로
                                        withContext(Dispatchers.Main) {
                                            if (isAdded && _binding != null) {
                                                updateBalanceUI(newBalance)
                                                Log.d(TAG, "이벤트 발생으로 잔액 업데이트: $newBalance")

                                                // 3. UI 강제 업데이트
                                                if (TransactionCache.isEmpty()) {
                                                    Log.d(TAG, "TransactionCache가 비어있음")
                                                } else {
                                                    Log.d(
                                                        TAG,
                                                        "거래 내역 업데이트: ${TransactionCache.size()}개"
                                                    )
                                                    val transactions =
                                                        TransactionCache.getRecentTransactions()

                                                    transactionAdapter =
                                                        TransactionAdapter(transactions)
                                                    binding.transactionList.adapter =
                                                        transactionAdapter
                                                    if (transactions.isNotEmpty()) {
                                                        binding.transactionList.scrollToPosition(0)
                                                    }
                                                    binding.transactionList.visibility =
                                                        View.VISIBLE
                                                }
                                                showLoading(false)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "이벤트 후 잔액 업데이트 실패", e)
                                    }
                                }
                            } else {
                                // Fragment가 더 이상 활성 상태가 아니라면 TransactionCache만 업데이트
                                TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "Fragment가 이미 제거됨, 캐시만 업데이트: ${transactionItem.title}")
                            }
                        }
                    }
                }, { error ->
                    Log.e(TAG, "충전 이벤트 오류: ${error.message}")
                    error.printStackTrace()
                })

            safeDisposables.add(SafeDisposable(depositDisposable))
            Log.d(TAG, "충전 이벤트 구독 설정 완료")
        } catch (e: Exception) {
            Log.e(TAG, "충전 이벤트 등록 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupWithdrawEventSubscription(bcm: BlockChainManager) {
        try {
            // 메인 스레드에서 네트워크 작업 수행하지 않도록 수정
            Log.d(TAG, "출금 이벤트 구독 시작")

            val withdrawDisposable = bcm.lectureEventMonitor.tokenWithdrawnEventFlowable(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(12345678L)),
                DefaultBlockParameterName.LATEST
            )
                .subscribe({ event ->
                    Log.d(
                        TAG,
                        "출금 이벤트 감지: userId=${event.userId}, 내 userId=$userId, 금액=${event.amount}"
                    )
                    if (event.userId == userId) {
                        Log.d(TAG, "내 출금 이벤트 감지: ${event.amount}")
                        val now = System.currentTimeMillis()
                        val date = getToday(now)
                        val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                        val transactionItem = TransactionItem("토큰 출금", date, convertedAmount, now)

                        // UI 스레드에서 처리
                        CoroutineScope(Dispatchers.Main).launch {
                            if (isAdded && _binding != null) {
                                // 1. 먼저 TransactionCache에 추가
                                val added = TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "트랜잭션 추가 결과: $added")

                                // 2. 잔액도 같이 새로고침 - IO 스레드에서 수행
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val newBalance = bcm.getMyCatTokenBalance()
                                        UserSession.lastKnownBalance = newBalance

                                        // UI 업데이트는 메인 스레드로
                                        withContext(Dispatchers.Main) {
                                            if (isAdded && _binding != null) {
                                                updateBalanceUI(newBalance)
                                                Log.d(TAG, "이벤트 발생으로 잔액 업데이트: $newBalance")

                                                // 3. UI 강제 업데이트
                                                val transactions =
                                                    TransactionCache.getRecentTransactions()
                                                transactionAdapter =
                                                    TransactionAdapter(transactions)
                                                binding.transactionList.adapter = transactionAdapter
                                                binding.transactionList.visibility = View.VISIBLE
                                                if (transactions.isNotEmpty()) {
                                                    binding.transactionList.scrollToPosition(0)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "이벤트 후 잔액 업데이트 실패", e)
                                    }
                                }
                            } else {
                                // Fragment가 더 이상 활성 상태가 아니라면 TransactionCache만 업데이트
                                TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "Fragment가 이미 제거됨, 캐시만 업데이트: ${transactionItem.title}")
                            }
                        }
                    }
                }, { error ->
                    Log.e(TAG, "출금 이벤트 오류: ${error.message}")
                    error.printStackTrace()
                })

            safeDisposables.add(SafeDisposable(withdrawDisposable))
            Log.d(TAG, "출금 이벤트 구독 설정 완료")
        } catch (e: Exception) {
            Log.e(TAG, "출금 이벤트 등록 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupPurchaseEventSubscription(bcm: BlockChainManager) {
        try {
            // 메인 스레드에서 네트워크 작업 수행하지 않도록 수정s
            Log.d(TAG, "강의 구매 이벤트 구독 시작")

            val purchaseDisposable = bcm.lectureEventMonitor.lecturePurchasedEventFlowable(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(12345678L)),
                DefaultBlockParameterName.LATEST
            )
                .subscribe({ event ->
                    Log.d(
                        TAG,
                        "강의 구매 이벤트 감지: userId=${event.userId}, 내 userId=$userId, 강의=${event.lectureTitle}, 금액=${event.amount}"
                    )
                    if (event.userId == userId) {
                        Log.d(TAG, "내 강의 구매 이벤트 감지: ${event.lectureTitle} - ${event.amount}")
                        val now = System.currentTimeMillis()
                        val date = getToday(now)
                        val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                        val transactionItem =
                            TransactionItem(event.lectureTitle, date, convertedAmount, now)

                        // UI 스레드에서 처리
                        CoroutineScope(Dispatchers.Main).launch {
                            if (isAdded && _binding != null) {
                                // 1. 먼저 TransactionCache에 추가
                                val added = TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "트랜잭션 추가 결과: $added")

                                // 2. 잔액도 같이 새로고침 - IO 스레드에서 수행
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val newBalance = bcm.getMyCatTokenBalance()
                                        UserSession.lastKnownBalance = newBalance

                                        // UI 업데이트는 메인 스레드로
                                        withContext(Dispatchers.Main) {
                                            if (isAdded && _binding != null) {
                                                updateBalanceUI(newBalance)
                                                Log.d(TAG, "이벤트 발생으로 잔액 업데이트: $newBalance")

                                                // 3. UI 강제 업데이트
                                                val transactions =
                                                    TransactionCache.getRecentTransactions()
                                                transactionAdapter =
                                                    TransactionAdapter(transactions)
                                                binding.transactionList.adapter = transactionAdapter
                                                binding.transactionList.visibility = View.VISIBLE
                                                if (transactions.isNotEmpty()) {
                                                    binding.transactionList.scrollToPosition(0)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "이벤트 후 잔액 업데이트 실패", e)
                                    }
                                }
                            } else {
                                // Fragment가 더 이상 활성 상태가 아니라면 TransactionCache만 업데이트
                                TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "Fragment가 이미 제거됨, 캐시만 업데이트: ${transactionItem.title}")
                            }
                        }
                    }
                }, { error ->
                    Log.e(TAG, "강의 구매 이벤트 오류: ${error.message}")
                    error.printStackTrace()
                })

            safeDisposables.add(SafeDisposable(purchaseDisposable))
            Log.d(TAG, "강의 구매 이벤트 구독 설정 완료")
        } catch (e: Exception) {
            Log.e(TAG, "강의 구매 이벤트 등록 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupSettlementEventSubscription(bcm: BlockChainManager) {
        try {
            // 메인 스레드에서 네트워크 작업 수행하지 않도록 수정
            Log.d(TAG, "정산 이벤트 구독 시작")

            val settleDisposable = bcm.lectureEventMonitor.lectureSettledEventFlowable(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(12345678L)),
                DefaultBlockParameterName.LATEST
            )
                .subscribe({ event ->
                    Log.d(
                        TAG,
                        "정산 이벤트 감지: participantId=${event.participantId}, 내 userId=$userId, 강의=${event.lectureTitle}, 금액=${event.amount}"
                    )
                    // 정산금을 받는 참가자 ID가 현재 사용자인지 확인
                    if (event.participantId == userId) {
                        Log.d(TAG, "내 정산 이벤트 감지: ${event.lectureTitle} - ${event.amount}")
                        val now = System.currentTimeMillis()
                        val date = getToday(now)
                        val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                        // "강의 수입: 강의명" 형식으로 표시
                        val transactionItem = TransactionItem(
                            "강의 수입: ${event.lectureTitle}",
                            date,
                            convertedAmount,
                            now
                        )

                        // UI 스레드에서 처리
                        CoroutineScope(Dispatchers.Main).launch {
                            if (isAdded && _binding != null) {
                                // 1. 먼저 TransactionCache에 추가
                                val added = TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "트랜잭션 추가 결과: $added")

                                // 2. 잔액도 같이 새로고침 - IO 스레드에서 수행
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val newBalance = bcm.getMyCatTokenBalance()
                                        UserSession.lastKnownBalance = newBalance

                                        // UI 업데이트는 메인 스레드로
                                        withContext(Dispatchers.Main) {
                                            if (isAdded && _binding != null) {
                                                updateBalanceUI(newBalance)
                                                Log.d(TAG, "이벤트 발생으로 잔액 업데이트: $newBalance")

                                                // 3. UI 강제 업데이트
                                                val transactions =
                                                    TransactionCache.getRecentTransactions()
                                                transactionAdapter =
                                                    TransactionAdapter(transactions)
                                                binding.transactionList.adapter = transactionAdapter
                                                binding.transactionList.visibility = View.VISIBLE
                                                if (transactions.isNotEmpty()) {
                                                    binding.transactionList.scrollToPosition(0)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "이벤트 후 잔액 업데이트 실패", e)
                                    }
                                }
                            } else {
                                // Fragment가 더 이상 활성 상태가 아니라면 TransactionCache만 업데이트
                                TransactionCache.addTransaction(transactionItem)
                                Log.d(TAG, "Fragment가 이미 제거됨, 캐시만 업데이트: ${transactionItem.title}")
                            }
                        }
                    }
                }, { error ->
                    Log.e(TAG, "정산 이벤트 오류: ${error.message}")
                    error.printStackTrace()
                })

            safeDisposables.add(SafeDisposable(settleDisposable))
            Log.d(TAG, "정산 이벤트 구독 설정 완료")
        } catch (e: Exception) {
            Log.e(TAG, "정산 이벤트 등록 실패: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addTransaction(transaction: TransactionItem) {
        Log.d(TAG, "트랜잭션 추가 시도: ${transaction.title}")

        val added = TransactionCache.addTransaction(transaction)

        if (added) {
            isDataLoaded = true

            // Fragment 유효성 체크 후 안전하게 UI 업데이트
            CoroutineScope(Dispatchers.Main).launch {
                // Fragment가 아직 활성 상태인지, 바인딩이 null이 아닌지 확인
                if (isAdded && _binding != null) {
                    Log.d(TAG, "UI 업데이트: 최신 거래 내역으로 RecyclerView 갱신")

                    // 모든 트랜잭션 표시
                    val transactions =
                        TransactionCache.getRecentTransactions(TransactionCache.size())
                    transactionAdapter = TransactionAdapter(transactions)
                    binding.transactionList.adapter = transactionAdapter

                    // 첫 번째 아이템으로 스크롤
                    if (transactions.isNotEmpty()) {
                        binding.transactionList.scrollToPosition(0)
                    }

                    showLoading(false)
                } else {
                    Log.d(TAG, "Fragment가 이미 제거되었거나 바인딩이 null입니다. UI 업데이트 건너뜀")
                }
            }
        } else {
            Log.d(TAG, "중복된 거래 무시: ${transaction.title}")
        }
    }

    private fun getToday(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy / MM / dd", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }


    private fun updateBalanceUI(balance: BigInteger) {
        val divisor = BigInteger.TEN.pow(18)
        val actualBalance = balance.divide(divisor)
        val formatter = DecimalFormat("#,###")
        val formattedBalance = "${formatter.format(actualBalance)} CAT"

        // 바인딩이 널이 아닌지 확인
        if (_binding != null) {
            binding.moneyCount.text = formattedBalance
            Log.d(TAG, "잔액 업데이트: $formattedBalance")
        } else {
            Log.w(TAG, "잔액 업데이트 실패: 바인딩이 null입니다")
        }
    }

    private fun showDefaultTransactions() {
        if (TransactionCache.isEmpty()) {
            // "거래 내역이 없습니다" 메시지를 보여주기 위한 어댑터 설정
            val emptyAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                inner class EmptyViewHolder(val textView: TextView) :
                    RecyclerView.ViewHolder(textView)

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): RecyclerView.ViewHolder {
                    val textView = TextView(requireContext()).apply {
                        text = "거래 내역이 없습니다."
                        textSize = 16f
                        gravity = Gravity.CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
                        setPadding(0, 50, 0, 50)
                    }
                    return EmptyViewHolder(textView)
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                }

                override fun getItemCount(): Int = 1
            }

            // RecyclerView에 어댑터 설정
            binding.transactionList.adapter = emptyAdapter
            binding.transactionList.visibility = View.VISIBLE
            Log.d(TAG, "거래 내역이 없음 메시지 표시됨")
        } else {
            // 기존 코드: 더미 거래 내역 표시
            val defaultTransactions = listOf(
                TransactionItem(
                    "데이터 분석 기초",
                    "2025 / 03 / 25",
                    BigInteger.valueOf(4),
                    System.currentTimeMillis()
                )
            )
            TransactionCache.updateTransactions(defaultTransactions)

            // 바인딩이 널이 아닌지 확인
            if (_binding != null) {
                transactionAdapter = TransactionAdapter(defaultTransactions)
                binding.transactionList.adapter = transactionAdapter
                binding.transactionList.visibility = View.VISIBLE
                Log.d(TAG, "기본 거래 내역 표시됨")
            } else {
                Log.w(TAG, "기본 거래 내역 표시 실패: 바인딩이 null입니다")
            }
        }
    }

    private fun showDefaultData() {
        // 바인딩이 널이 아닌지 확인
        if (_binding != null) {
            binding.moneyCount.text = "loading..."
            showDefaultTransactions()
            Log.d(TAG, "기본 데이터 표시됨")
        } else {
            Log.w(TAG, "기본 데이터 표시 실패: 바인딩이 null입니다")
        }
    }

    private fun showLoading(loading: Boolean) {
        isLoading = loading

        // 바인딩이 널이 아닌지 확인
        if (_binding != null) {
            if (loading) {
                binding.transactionList.visibility = View.INVISIBLE
                binding.loadingSpinner.visibility = View.VISIBLE
                binding.loadingText.visibility = View.VISIBLE
                binding.blockTouchOverlay.visibility = View.VISIBLE
                binding.chargeBtn.isEnabled = false
            } else {
                binding.transactionList.visibility = View.VISIBLE
                binding.loadingSpinner.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
                binding.blockTouchOverlay.visibility = View.GONE
                binding.chargeBtn.isEnabled = true
            }
        } else {
            Log.w(TAG, "로딩 상태 변경 실패: 바인딩이 null입니다")
        }
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 활성화될 때마다 데이터 새로고침 검사
        val currentTime = System.currentTimeMillis()
        val needsRefresh = lastRefreshTime == 0L || currentTime - lastRefreshTime > REFRESH_INTERVAL

        if (needsRefresh) {
            // 주기적인 새로고침이 필요한 경우에만 데이터 다시 로드
            CoroutineScope(Dispatchers.Main).launch {
                loadBlockchainData(true)
                lastRefreshTime = currentTime
            }
        }
    }

    override fun onDestroyView() {
        // 백그라운드에서 안전하게 dispose 호출
        CoroutineScope(Dispatchers.IO).launch {
            for (disposable in safeDisposables) {
                disposable.dispose()
            }
            Log.d(TAG, "onDestroyView에서 이벤트 구독 취소됨")
        }

        super.onDestroyView()
        _binding = null
    }
}

class SafeDisposable(private val disposable: io.reactivex.rxjava3.disposables.Disposable) {
    fun dispose() {
        // IO 스레드에서 안전하게 dispose
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!disposable.isDisposed) {
                    disposable.dispose()
                }
            } catch (e: Exception) {
                Log.e("SafeDisposable", "Dispose 오류: ${e.message}")
            }
        }
    }

    val isDisposed: Boolean
        get() = disposable.isDisposed
}