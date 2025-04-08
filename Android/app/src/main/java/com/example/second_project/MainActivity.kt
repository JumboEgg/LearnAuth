package com.example.second_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.second_project.databinding.ActivityMainBinding
import com.example.second_project.utils.ApiKeyProvider

private const val TAG = "MainActivity_야옹"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 로그인 상태 확인
//        if (UserSession.userId == 0) {
//            // 로그인되지 않은 경우 로그인 화면으로 이동
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            return
//        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네비게이션 컨트롤러 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 딥링크 처리
        handleIntent(intent)

        // nav 바 없어져야 하는 페이지
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.certDetailFragment
                    ,R.id.registerMainFragment
                    , R.id.lectureDetailFragment
                    , R.id.ownedLectureDetailFragment
                    , R.id.lecturePlayFragment
                    , R.id.chargeFragment
                    , R.id.myWalletFragment
                    , R.id.declarationFragment
                    , R.id.myLectureFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }

        binding.bottomNavigation.setupWithNavController(navController)

        // lifecycleScope.launch {
        //     try {
        //         // userId를 BigInteger로 변환해서 전달
        //         //userId는 기본값 1로 둡니다. 아직 로그인 로직이 없으므로...
        //         blockchainManager.getTransactionHistory(BigInteger.valueOf(1))
        //         Log.d(TAG, "onCreate: 거래내역 로딩 완료 ")
        //     } catch (e: Exception) {
        //         Log.e("MainActivity", "Error fetching transanction history", e)
        //     }
        // }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri: Uri? = intent.data
            if (uri != null) {
                Log.d(TAG, "딥링크 처리: $uri")
                
                // URI에서 토큰 ID 추출
                val tokenId = uri.getQueryParameter("tokenId")
                if (tokenId != null) {
                    Log.d(TAG, "토큰 ID 추출: $tokenId")
                    
                    // 수료증 검증 화면으로 이동
                    navController.navigate(
                        R.id.action_global_certVerifyFragment,
                        Bundle().apply {
                            putString("tokenId", tokenId)
                        }
                    )
                }
            }
        }
    }
}