package com.example.second_project.data

import android.util.Log
import java.math.BigInteger
import java.util.Collections

data class TransactionItem(
    val title: String,
    val date: String,         // 화면에 표시할 형식의 날짜 문자열
    val amount: BigInteger,
    val timestamp: Long       // 정렬과 중복 확인에 사용하는 타임스탬프
)

object TransactionCache {
    private const val TAG = "TransactionCache"

    // 앱 실행 중 유지되는 거래 내역 리스트 (thread-safe를 위해 Collections.synchronizedList 사용)
    private var _transactions: MutableList<TransactionItem> =
        Collections.synchronizedList(mutableListOf())

    // 읽기 전용 트랜잭션 리스트 (외부에서 읽기만 가능)
    val transactions: List<TransactionItem>
        get() = synchronized(_transactions) { _transactions.toList() }

    // 마지막 갱신 시각
    var lastUpdateTime: Long = 0
        private set  // 외부에서는 읽기만 가능하도록 설정

    // 중복 거래 체크 (중복이면 true 반환)
    fun isDuplicate(newTransaction: TransactionItem): Boolean {
        return synchronized(_transactions) {
            _transactions.any { existing ->
                // 제목이 같고, 금액이 같고, 타임스탬프가 1초 이내로 가까우면 중복으로 간주
                existing.title == newTransaction.title &&
                        existing.amount == newTransaction.amount &&
                        Math.abs(existing.timestamp - newTransaction.timestamp) < 1000
            }
        }
    }

    // 거래 내역 추가 (중복 확인) - 수정: 동기화 블록 사용 및 정렬 보장
    fun addTransaction(transaction: TransactionItem): Boolean {
        if (isDuplicate(transaction)) {
            Log.d(TAG, "중복된 거래 무시됨: ${transaction.title} (${transaction.amount})")
            return false
        }

        synchronized(_transactions) {
            // 현재 시간을 정확하게 사용하고 로그에 기록
            val currentTimestamp = System.currentTimeMillis()
            val newTransaction = transaction.copy(timestamp = currentTimestamp)
            Log.d(TAG, "새 거래 추가: ${newTransaction.title}, 시각: ${currentTimestamp}")

            // 리스트에 추가
            _transactions.add(newTransaction)

            // 명시적으로 타임스탬프 내림차순 정렬
            _transactions.sortByDescending { it.timestamp }

            // 정렬 결과 로깅 (디버깅용)
            if (_transactions.isNotEmpty()) {
                Log.d(TAG, "정렬 후 첫 5개 트랜잭션:")
                _transactions.take(Math.min(5, _transactions.size)).forEachIndexed { index, tx ->
                    Log.d(TAG, "[$index] ${tx.title}: ${tx.timestamp}")
                }
            }

            lastUpdateTime = System.currentTimeMillis()
            Log.d(
                TAG,
                "새 거래 추가됨: ${newTransaction.title} (${newTransaction.amount}), 총 ${_transactions.size}개"
            )
        }
        return true
    }

    // 모든 거래 내역 설정 (기존 내역 대체) - 수정: 동기화 및 정렬 보장
    fun updateTransactions(newTransactions: List<TransactionItem>) {
        synchronized(_transactions) {
            // 명시적으로 타임스탬프로 정렬한 새 리스트 생성
            val sortedTransactions = newTransactions
                .sortedByDescending { it.timestamp }
                .toMutableList()

            // 정렬 결과 로깅
            if (sortedTransactions.isNotEmpty()) {
                Log.d(TAG, "업데이트 후 첫 3개 트랜잭션 타임스탬프:")
                sortedTransactions.take(Math.min(3, sortedTransactions.size))
                    .forEachIndexed { index, tx ->
                        Log.d(TAG, "[$index] ${tx.title}: ${tx.timestamp}")
                    }
            }

            // 정렬된 리스트로 교체
            _transactions.clear()
            _transactions.addAll(sortedTransactions)

            lastUpdateTime = System.currentTimeMillis()
            Log.d(TAG, "거래 내역 갱신됨: 총 ${_transactions.size}개")
        }
    }

    // 최근 N개 거래 내역 가져오기 - 수정: 동기화 및 명시적 정렬 보장
    fun getRecentTransactions(count: Int = 10): List<TransactionItem> {
        return synchronized(_transactions) {
            // 항상 정렬을 보장하기 위해 다시 정렬
            _transactions.sortByDescending { it.timestamp }

            // _transactions에서 직접 추출 (transactions 프로퍼티 대신)
            val result = _transactions.take(Math.min(count, _transactions.size))

            // 정렬 확인을 위한 로깅
            if (result.isNotEmpty()) {
                Log.d(TAG, "getRecentTransactions 결과 (첫 3개):")
                result.take(Math.min(3, result.size)).forEachIndexed { index, tx ->
                    Log.d(TAG, "[$index] ${tx.title}: ${tx.timestamp}, ${tx.date}")
                }
            }

            result
        }
    }

    // 캐시 초기화
    fun clear() {
        synchronized(_transactions) {
            _transactions.clear()
            lastUpdateTime = 0
            Log.d(TAG, "거래 내역 캐시 초기화됨")
        }
    }

    // 데이터가 상대적으로 최신인지 확인 (지정된 시간 내 갱신되었는지) - 10초
    fun isFresh(maxAgeMs: Long = 10 * 1000): Boolean {
        if (lastUpdateTime == 0L) return false
        return System.currentTimeMillis() - lastUpdateTime < maxAgeMs
    }

    // 현재 트랜잭션이 비어있는지 확인하는 유틸리티 함수
    fun isEmpty(): Boolean = synchronized(_transactions) { _transactions.isEmpty() }

    // 트랜잭션 개수 반환하는 유틸리티 함수
    fun size(): Int = synchronized(_transactions) { _transactions.size }
}