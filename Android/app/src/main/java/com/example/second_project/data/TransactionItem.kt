package com.example.second_project.data

import android.util.Log
import java.math.BigInteger
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 트랜잭션 캐시를 관리하는 싱글톤 객체
 * 중복 거래 내역을 방지하고 효율적인 캐싱을 제공합니다.
 */
object TransactionCache {
    private const val TAG = "TransactionCache"

    // 스레드 안전한 CopyOnWriteArrayList 사용
    private val transactions = CopyOnWriteArrayList<TransactionItem>()

    // 마지막 업데이트 시간 추적
    var lastUpdateTime = 0L
        private set

    // 캐시가 신선한지 확인하는 기본 기간 (2분)
    private const val DEFAULT_CACHE_FRESHNESS_PERIOD = 2 * 60 * 1000L

    /**
     * 거래 내역 목록을 업데이트합니다. 중복은 제거됩니다.
     * @param newTransactions 새 거래 내역 목록
     */
    @Synchronized
    fun updateTransactions(newTransactions: List<TransactionItem>) {
        Log.d(TAG, "updateTransactions: 새 거래 ${newTransactions.size}개 시작")

        if (newTransactions.isEmpty()) return

        // 기존 거래와 새 거래를 병합 (중복 제거)
        val updatedList = mutableListOf<TransactionItem>()

        // 먼저 새 거래 추가
        for (newTx in newTransactions) {
            if (!containsTransaction(updatedList, newTx)) {
                updatedList.add(newTx)
            }
        }

        // 기존 거래 추가 (중복 제외)
        for (existingTx in transactions) {
            if (!containsTransaction(updatedList, existingTx)) {
                updatedList.add(existingTx)
            }
        }

        // 타임스탬프 기준으로 정렬 (내림차순)
        val sortedList = updatedList.sortedByDescending { it.timestamp }

        // 기존 목록 지우고 정렬된 결과로 교체
        transactions.clear()
        transactions.addAll(sortedList)

        // 마지막 업데이트 시간 기록
        lastUpdateTime = System.currentTimeMillis()

        Log.d(TAG, "updateTransactions: 최종 거래 내역 ${transactions.size}개 (중복 제거 후)")
    }

    /**
     * 새 트랜잭션을 추가합니다. 중복이면 추가하지 않습니다.
     * @param transaction 추가할 거래 내역
     * @return 성공적으로 추가되었으면 true, 중복이라 추가되지 않았으면 false
     */
    @Synchronized
    fun addTransaction(transaction: TransactionItem): Boolean {
        // 이미 같은 거래가 있는지 확인
        val isDuplicate = transactions.any { existingTx ->
            isSameTransaction(existingTx, transaction)
        }

        if (!isDuplicate) {
            // 새 거래 추가하고 타임스탬프 기준으로 다시 정렬
            transactions.add(transaction)
            val sortedList = transactions.sortedByDescending { it.timestamp }

            transactions.clear()
            transactions.addAll(sortedList)

            lastUpdateTime = System.currentTimeMillis()
            Log.d(TAG, "addTransaction: 새 거래 추가됨 - ${transaction.title}, 총 ${transactions.size}개")
            return true
        }

        Log.d(TAG, "addTransaction: 중복 거래 무시됨 - ${transaction.title}")
        return false
    }

    /**
     * 지정된 수만큼 최근 거래 내역을 반환합니다.
     * @param count 반환할 거래 내역 수 (기본값은 전체)
     * @return 최근 거래 내역 리스트
     */
    fun getRecentTransactions(count: Int = transactions.size): List<TransactionItem> {
        val result = if (count >= transactions.size) {
            transactions.toList()
        } else {
            transactions.take(count).toList()
        }

        return result
    }

    /**
     * 캐시가 비어있는지 확인합니다.
     * @return 캐시가 비어있으면 true, 그렇지 않으면 false
     */
    fun isEmpty(): Boolean = transactions.isEmpty()

    /**
     * 캐시된 거래 내역 수를 반환합니다.
     * @return 캐시된 거래 내역 수
     */
    fun size(): Int = transactions.size

    /**
     * 캐시가 신선한지 확인합니다.
     * @param freshnessPeriod 신선함을 확인할 기간 (밀리초). 기본값은 2분입니다.
     * @return 신선하면 true, 그렇지 않으면 false
     */
    fun isFresh(freshnessPeriod: Long = DEFAULT_CACHE_FRESHNESS_PERIOD): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastUpdateTime < freshnessPeriod
    }

    /**
     * 두 트랜잭션이 동일한지 확인합니다.
     * 거래 제목, 날짜, 금액이 동일하면 같은 거래로 간주합니다.
     * @param tx1 첫 번째 거래
     * @param tx2 두 번째 거래
     * @return 동일하면 true, 그렇지 않으면 false
     */
    private fun isSameTransaction(tx1: TransactionItem, tx2: TransactionItem): Boolean {
        // 제목, 날짜, 금액이 같으면 동일한 거래로 간주
        return tx1.title == tx2.title &&
                tx1.date == tx2.date &&
                tx1.amount.compareTo(tx2.amount) == 0
    }

    /**
     * 리스트에 동일한 거래가 있는지 확인합니다.
     * @param list 확인할 리스트
     * @param transaction 검색할 거래
     * @return 리스트에 동일한 거래가 있으면 true, 그렇지 않으면 false
     */
    private fun containsTransaction(
        list: List<TransactionItem>,
        transaction: TransactionItem
    ): Boolean {
        return list.any { existingTx -> isSameTransaction(existingTx, transaction) }
    }

    /**
     * 캐시를 초기화합니다.
     */
    fun clear() {
        transactions.clear()
        lastUpdateTime = 0L
        Log.d(TAG, "clear: 캐시가 초기화되었습니다.")
    }
}

/**
 * 거래 내역 항목 데이터 클래스
 */
data class TransactionItem(
    val title: String,
    val date: String,
    val amount: BigInteger,
    val timestamp: Long
)