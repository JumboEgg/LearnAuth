package com.example.second_project.blockchain

import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

class HighGasProvider : ContractGasProvider {
    override fun getGasPrice(contractFunc: String?): BigInteger {
        // 테스트넷에서는 적절한 가스 가격 사용
        return BigInteger.valueOf(25_000_000_000L) // 25 Gwei
    }

    override fun getGasPrice(): BigInteger {
        return getGasPrice(null)
    }

    override fun getGasLimit(contractFunc: String?): BigInteger {
        // 테스트넷에서는 적절한 가스 한도 사용
        return BigInteger.valueOf(3_000_000) // 3,000,000
    }

    override fun getGasLimit(): BigInteger {
        return getGasLimit(null)
    }
}
