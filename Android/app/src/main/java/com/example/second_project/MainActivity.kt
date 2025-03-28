package com.example.second_project

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.setupWithNavController
import com.example.second_project.blockchain.BlockchainManager
import com.example.second_project.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.math.BigInteger
import io.reactivex.rxjava3.core.Flowable

private const val TAG = "MainActivity_야옹"
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var blockchainManager: BlockchainManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blockchainManager = BlockchainManager()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment

        val navController = navHostFragment.navController
        
        // nav 바 없어져야 하는 페이지
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.certDetailFragment,R.id.registerMainFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }

        binding.bottomNavigation.setupWithNavController(navController)

        lifecycleScope.launch {
            try {
                // userId를 BigInteger로 변환해서 전달
                //userId는 기본값 1로 둡니다. 아직 로그인 로직이 없으므로...
                blockchainManager.getTransactionHistory(BigInteger.valueOf(1))
                Log.d(TAG, "onCreate: 거래내역 로딩 완료 ")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching transanction history", e)
            }
        }

    }
}