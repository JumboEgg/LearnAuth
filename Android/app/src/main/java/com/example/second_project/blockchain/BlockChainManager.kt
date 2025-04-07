package com.example.second_project.blockchain

import android.util.Log
import com.example.second_project.blockchain.monitor.LectureSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import java.io.File
import java.math.BigInteger

private const val TAG = "BlockChainManager_야옹"

class BlockChainManager(
    private val walletPassword: String,
    private val walletFile: File
) {

    val web3j: Web3j = Web3j.build(HttpService("https://rpc-amoy.polygon.technology/"))
    val credentials: Credentials = WalletUtils.loadCredentials(walletPassword, walletFile)

    // ✅ EIP-155 적용된 트랜잭션 매니저 사용
    private val chainId = 80002L // Polygon Amoy 테스트넷
    private val txManager: TransactionManager = RawTransactionManager(web3j, credentials, chainId)
    
    // 고가스 제공자 사용
    private val gasProvider = HighGasProvider()

    val lectureSystem: LectureSystem
    val catToken: CATToken
    val forwarder: LectureForwarder

    init {
        val addresses = mapOf(
            "LectureForwarder" to "0x8424d5F766121B16c0d2F5d0cf8aC4594aC62Fe8",
            "CATToken" to "0x02078287108e640e6Cc2da073870763970E08e95",
            "LectureSystem" to "0xeE2dD174b049953495A246A5197E3e1D9929000D"
        )

        // ✅ txManager로 EIP-155 트랜잭션 실행
        lectureSystem = LectureSystem.load(
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

    suspend fun getTransactionHistory(userId: BigInteger) {
        withContext(Dispatchers.IO) {
            // 이벤트 구독
            lectureSystem.tokenDepositedEventFlowable(
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

            lectureSystem.tokenWithdrawnEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(
                { event ->
                    if (event.userId == userId) {
                        Log.d(TAG, "💸 Withdraw: ${event.amount}, ${event.activityType}")
                    }
                },
                { error -> Log.e(TAG, "Error fetching withdrawals", error) }
            )

            lectureSystem.lecturePurchasedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(
                { event ->
                    if (event.userId == userId) {
                        Log.d(TAG, "🎓 Purchase: ${event.amount}, ${event.lectureTitle}")
                    }
                },
                { error -> Log.e(TAG, "Error fetching purchases", error) }
            )
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
