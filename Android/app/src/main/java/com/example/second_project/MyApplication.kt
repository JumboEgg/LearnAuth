package com.example.second_project

import android.app.Application
import android.util.Log
import io.reactivex.plugins.RxJavaPlugins  // 이 import 추가
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupBouncyCastle()
        setupRxJavaErrorHandler()  // 추가된 메소드
        UserSession.init(this)
    }

    private fun setupBouncyCastle() {
        // Android 내장 BC Provider가 있다면 제거하고 공식 BC Provider 등록
        val provider = Security.getProvider("BC")
        if (provider == null || provider.javaClass.name == "com.android.org.bouncycastle.jce.provider.BouncyCastleProvider") {
            Security.removeProvider("BC")
            Security.insertProviderAt(BouncyCastleProvider(), 1)
            Log.d("MyApplication", "BouncyCastle provider successfully inserted")
        }
    }

    private fun setupRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable ->
            Log.e("RxJava", "Undeliverable exception ignored: ${throwable.message}")
            // 여기서 오류를 무시하고 앱 충돌을 방지합니다
        }
    }
}