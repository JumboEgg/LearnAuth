package com.example.second_project.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.second_project.R
import com.example.second_project.UserSession
import com.example.second_project.adapter.TransactionAdapter
import com.example.second_project.blockchain.BlockChainManager
import com.example.second_project.data.TransactionItem
import com.example.second_project.databinding.FragmentMywalletBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class MyWalletFragment : Fragment() {
    private var _binding: FragmentMywalletBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private val TAG = "MyWalletFragment_야옹"

    // 블록체인 매니저
    private var blockChainManager: BlockChainManager? = null

    // 트랜잭션 목록을 스레드 안전하게 관리
    private val transactions = CopyOnWriteArrayList<TransactionItem>()
    private var isDataLoaded = false

    // 사용자 ID
    private val userId: BigInteger
        get() = BigInteger.valueOf(UserSession.userId.toLong())

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

        // UserId 로깅 추가
        Log.d(TAG, "내 userId: $userId (decimal), 0x${userId.toString(16)} (hex)")

        // RecyclerView 초기화
        transactionAdapter = TransactionAdapter(emptyList())
        binding.transactionList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }

        // UserSession에서 블록체인 매니저 가져오기
        blockChainManager = UserSession.getBlockchainManagerIfAvailable(requireContext())

        // 사용자 이름 표시
        binding.userName.text = UserSession.name ?: "사용자"

        // 블록체인 데이터 로드
        loadBlockchainData()

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

    private fun loadBlockchainData() {
        // 화면에 로딩 표시
        showLoading(true)

        if (blockChainManager == null) {
            Log.e(TAG, "BlockChainManager가 초기화되지 않았습니다")
            showDefaultData()
            showLoading(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 캐시된 잔액 사용 (빠른 응답)
                val cachedBalance = UserSession.lastKnownBalance
                if (cachedBalance != null) {
                    withContext(Dispatchers.Main) { updateBalanceUI(cachedBalance) }
                }

                // 2. 최신 잔액 조회 및 UI 업데이트
                val actualBalance = blockChainManager!!.getMyCatTokenBalance()
                UserSession.lastKnownBalance = actualBalance
                withContext(Dispatchers.Main) { updateBalanceUI(actualBalance) }

                // 3. 역사적 거래 내역 로드 (기존 이벤트 조회)
                loadHistoricalEvents()

                // 4. 실시간 이벤트 구독 (구독 도중에는 기본 데이터 호출을 건너뛰도록 플래그를 업데이트)
                setupTransactionEvents()

                // 5. 일정 시간 후에도 거래 내역이 없으면 기본 데이터 표시
                // (예를 들어, 5초 대기)
                kotlinx.coroutines.delay(5000)
                withContext(Dispatchers.Main) {
                    // 만약 이미 실시간 이벤트나 역사적 이벤트로 인해 데이터가 채워졌다면 기본 데이터 호출하지 않음
                    if (transactions.isEmpty() && !isDataLoaded) {
                        showDefaultTransactions()
                    }
                    showLoading(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "블록체인 데이터 로드 오류: ${e.message}")
                withContext(Dispatchers.Main) {
                    showDefaultData()
                    showLoading(false)
                }
            }
        }
    }


    private suspend fun loadHistoricalEvents() {
        val bcm = blockChainManager ?: return
        val web3j = bcm.web3j
        val contract = bcm.lectureSystem
        val address = contract.contractAddress

        val startBlock = DefaultBlockParameter.valueOf(
            BigInteger.valueOf( // 배포 블록이나 과거 특정 블록 번호
                12345678L
            )
        )
        val endBlock = DefaultBlockParameterName.LATEST
        val events = listOf(
            contract.getEventLogs("TokenDeposited", userId, web3j, address, startBlock, endBlock)
                .map {
                    TransactionItem("토큰 충전", it.date, it.amount)
                },
            contract.getEventLogs("TokenWithdrawn", userId, web3j, address, startBlock, endBlock)
                .map {
                    TransactionItem("토큰 출금", it.date, it.amount)
                },
            contract.getEventLogs("LecturePurchased", userId, web3j, address, startBlock, endBlock)
                .map {
                    TransactionItem(it.title, it.date, it.amount)
                }
        ).flatten()

        withContext(Dispatchers.Main) {
            if (events.isNotEmpty()) {
                Log.d(TAG, "📜 과거 트랜잭션 ${events.size}개 로드됨")
                transactions.addAll(events.sortedByDescending { it.date })
                transactionAdapter = TransactionAdapter(transactions.take(10))
                binding.transactionList.adapter = transactionAdapter
                isDataLoaded = true
            }
        }
    }


    private suspend fun setupTransactionEvents() {
        val bcm = blockChainManager ?: return

        // 1. 충전 이벤트
        try {
            bcm.lectureSystem.tokenDepositedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe({ event ->
                if (event.userId == userId) {
                    Log.d(TAG, "✅ 충전 이벤트 매치됨! 금액: ${event.amount}")
                    val date = getToday()
                    val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                    val transactionItem = TransactionItem("토큰 충전", date, convertedAmount)
                    addTransaction(transactionItem)
                }
            }, { error -> Log.e(TAG, "충전 이벤트 오류: ${error.message}") })
        } catch (e: Exception) {
            Log.e(TAG, "충전 이벤트 등록 실패: ${e.message}")
        }

        // 2. 출금 이벤트
        try {
            bcm.lectureSystem.tokenWithdrawnEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe({ event ->
                if (event.userId == userId) {
                    Log.d(TAG, "✅ 출금 이벤트 매치됨! 금액: ${event.amount}")
                    val date = getToday()
                    val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                    val transactionItem = TransactionItem("토큰 출금", date, convertedAmount)
                    addTransaction(transactionItem)
                }
            }, { error -> Log.e(TAG, "출금 이벤트 오류: ${error.message}") })
        } catch (e: Exception) {
            Log.e(TAG, "출금 이벤트 등록 실패: ${e.message}")
        }

        // 3. 강의 구매 이벤트
        try {
            bcm.lectureSystem.lecturePurchasedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe({ event ->
                if (event.userId == userId) {
                    Log.d(TAG, "✅ 강의 구매 이벤트 매치됨! 제목: ${event.lectureTitle}, 금액: ${event.amount}")
                    val date = getToday()
                    val convertedAmount = event.amount.divide(BigInteger.TEN.pow(18))
                    // 강의 구매일 때는 강의 제목이 그대로 title로 들어감
                    val transactionItem = TransactionItem(event.lectureTitle, date, convertedAmount)
                    addTransaction(transactionItem)
                }
            }, { error -> Log.e(TAG, "강의 구매 이벤트 오류: ${error.message}") })
        } catch (e: Exception) {
            Log.e(TAG, "강의 구매 이벤트 등록 실패: ${e.message}")
        }

        // (선택적) 이력 조회는 그대로
        try {
            bcm.getTransactionHistory(userId)
        } catch (e: Exception) {
            Log.e(TAG, "거래 이력 조회 실패: ${e.message}")
        }
    }

    private fun getToday(): String {
        val dateFormat = SimpleDateFormat("yyyy / MM / dd", Locale.getDefault())
        return dateFormat.format(Date(System.currentTimeMillis()))
    }


    private fun addTransaction(transaction: TransactionItem) {
        // 거래 내역 추가
        transactions.add(0, transaction)
        // 실시간 데이터가 있으므로 플래그 업데이트
        isDataLoaded = true

        CoroutineScope(Dispatchers.Main).launch {
            // 여기서 adapter에 이미 기본 거래 내역이 존재한다면, 새 거래 내역과 합쳐서 다시 갱신
            transactionAdapter = TransactionAdapter(transactions.take(10))
            binding.transactionList.adapter = transactionAdapter
            showLoading(false)
        }
    }


    private fun updateBalanceUI(balance: BigInteger) {
        // 블록체인에서는 값이 10^18 단위로 저장되므로 나눠줘야 함
        val divisor = BigInteger.TEN.pow(18) // 10^18
        val actualBalance = balance.divide(divisor)

        // CAT 잔액 포맷팅 및 표시
        val formatter = DecimalFormat("#,###")
        val formattedBalance = "${formatter.format(actualBalance)} CAT"

        Log.d(TAG, "잔액 업데이트: 원본=$balance, 변환된 값=$actualBalance")

        binding.moneyCount.text = formattedBalance
    }

    private fun showDefaultTransactions() {
        if (transactions.isEmpty()) {
            // 기본 데이터에는 일반 숫자 값을 사용 (이미 10^18로 나눈 값)
            val defaultTransactions = listOf(
                TransactionItem("데이터 분석 기초", "2025 / 03 / 25", BigInteger.valueOf(4)),
                TransactionItem("일상 생활 관리", "2025 / 03 / 24", BigInteger.valueOf(30)),
                TransactionItem("기본 법률 상식", "2025 / 03 / 23", BigInteger.valueOf(55)),
                TransactionItem("스포츠 심리학", "2025 / 03 / 22", BigInteger.valueOf(6)),
                TransactionItem("마케팅 전략", "2025 / 03 / 21", BigInteger.valueOf(4))
            )

            transactionAdapter = TransactionAdapter(defaultTransactions)
            binding.transactionList.adapter = transactionAdapter

            Log.d(TAG, "기본 거래 내역 표시됨")
        }
    }

    private fun showDefaultData() {
        // 기본 데이터로 UI 업데이트
        binding.moneyCount.text = "55 CAT"
        showDefaultTransactions()

        Log.d(TAG, "기본 데이터 표시됨")
    }

    // 로딩 시작 시
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.transactionList.visibility = View.INVISIBLE
            binding.loadingSpinner.visibility = View.VISIBLE
            binding.blockTouchOverlay.visibility = View.VISIBLE  // 터치 차단 오버레이 활성화
            binding.chargeBtn.isEnabled = false                // 버튼 클릭 비활성화
        } else {
            binding.transactionList.visibility = View.VISIBLE
            binding.loadingSpinner.visibility = View.GONE
            binding.blockTouchOverlay.visibility = View.GONE    // 터치 차단 오버레이 비활성화
            binding.chargeBtn.isEnabled = true                 // 버튼 클릭 활성화
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}