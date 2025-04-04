package com.example.second_project.blockchain

import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

class HighGasProvider : ContractGasProvider {
    override fun getGasPrice(contractFunc: String?): BigInteger {
        // Base fee + priority fee = 25~30 Gwei 추천
        return BigInteger.valueOf(30_000_000_000L) // 30 Gwei
    }

    override fun getGasPrice(): BigInteger {
        return getGasPrice(null)
    }

    override fun getGasLimit(contractFunc: String?): BigInteger {
        return BigInteger.valueOf(500_000) // 여유 있게
    }

    override fun getGasLimit(): BigInteger {
        return getGasLimit(null)
    }
}
