package com.example.second_project

import android.app.Application
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupBouncyCastle()
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
}
