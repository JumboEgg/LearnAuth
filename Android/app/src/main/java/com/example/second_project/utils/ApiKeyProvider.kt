package com.example.second_project.utils

import android.content.Context
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

object ApiKeyProvider {
    // 간단한 XOR 암호화 사용
    private const val KEY = "MySecretKey123456" // 16자리 이상의 임의의 문자열
    private const val ENCRYPTED_API_KEY = "DxwyFwYARREyLxFTdVBdel8HMAYfKkMrHQIWMF9gBld2f3swOBU7JCY+ckscSHgCVwdgNCoEUA4QVj4/PCFjQlEGAV8CFyAMAiU0HQQMM1p9ZHJceycVOCsgQwguISAOfWZiTnhbAA0KCCkaPyd6DCN1eFl6X2AnNgQgGT8PEyIpOnteUWNzRi86GlMqHCccESJBBHxnUwBnCh0nPDQeFjgmKw9TYXpHfFsbDQoyDwE9RxEJGlxeXlViYCYwORVTEQsiJyk6e0VSYwBQLj5qFgIlK0ECDwkGe11+WWx/FSUHDT8MOycRTnhfYVhWBCEACTIxIT8sCRYYZnxbUHJaOxs4KxUWMkF7LBNeSn93f0YXOhpTKhk/JxoxPFhUYEwCfyArPwZRHhwuHDcqa2pxR1RhAxE3Ig8EBx8FEx1mBwN9X1k1NRAvEygmPX0sEgRoYk5wXysvYxYqHD8YKAs3QVABAFx5Jz9qKSA4ES4mIx9rZQZcbFs1FQkmKkQ/GQ0WGgNnQH1beH0gCzdSERw9fSwSd3ZlcVlhHyoZXC8xLxwvPSteaGQBBVcaNzsBJB4TFic3TFJ1Zl16XwcDCldaBT8jGSkjaVlaeHZ8NyBhXBQoMiYHPyFdfmlsXl8CEBpVLQgOQAQyLANoWWZZbAosZj8JFVYtIQIAaEt6R3xYAxMxViEePzE/CRxnfF9tBnwhHRAsVTsIPXsrE3QGaV5zXQMDOFE5JjwDEQ8rWWtkcQB7NzRhPAkaDDohJEt/dnlYb3IUAx0fLRsqIBkPN2VeW3pyczQ0Bz8KPzI9fj89UkV+cHxOFxMeVC4YAh0HJjNdV3t1XHknPGArGScdOw8gSH9IXw0bbH0pCwxUHTUfPSERS2ULXAcEJhUgEBokNEIUKiBySh4EbQEINmo9Vy0XGw=="

    fun encryptApiKey(apiKey: String): String {
        val keyBytes = KEY.toByteArray()
        val apiKeyBytes = apiKey.toByteArray()
        val result = ByteArray(apiKeyBytes.size)

        for (i in apiKeyBytes.indices) {
            result[i] = (apiKeyBytes[i] xor keyBytes[i % keyBytes.size])
        }
        return Base64.encodeToString(result, Base64.DEFAULT)
    }

    fun getPinataApiKey(): String {
        return try {
            val encryptedBytes = Base64.decode(ENCRYPTED_API_KEY, Base64.DEFAULT)
            val keyBytes = KEY.toByteArray()
            val result = ByteArray(encryptedBytes.size)

            for (i in encryptedBytes.indices) {
                result[i] = (encryptedBytes[i] xor keyBytes[i % keyBytes.size])
            }
            String(result)
        } catch (e: Exception) {
            ""
        }
    }
}