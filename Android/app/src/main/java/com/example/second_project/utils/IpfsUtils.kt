package com.example.second_project.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.second_project.network.PinataApiClient
import com.example.second_project.network.PinataResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import retrofit2.Response

object IpfsUtils {
    private const val TAG = "IpfsUtils"

    /**
     * Uri에서 파일을 가져와 IPFS에 업로드합니다.
     * @param context 컨텍스트
     * @param uri 업로드할 파일의 Uri
     * @param apiKey Pinata API 키
     * @return IPFS 해시 또는 null (실패 시)
     */
    suspend fun uploadFileToIpfs(context: Context, uri: Uri, apiKey: String): String? {
        try {
            // Uri에서 파일 생성
            val file = createTempFileFromUri(context, uri) ?: return null
            
            // MultipartBody.Part 생성
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            // Pinata API 호출
            val response = PinataApiClient.pinataService.pinFileToIPFS(apiKey, body)
            
            // 응답 처리
            if (response.isSuccessful) {
                val ipfsHash = response.body()?.IpfsHash
                Log.d(TAG, "파일 업로드 성공: $ipfsHash")
                return ipfsHash
            } else {
                Log.e(TAG, "파일 업로드 실패: ${response.code()} - ${response.message()}")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "파일 업로드 중 오류 발생", e)
            return null
        }
    }

    /**
     * JSON 데이터를 IPFS에 업로드합니다.
     * @param apiKey Pinata API 키
     * @param jsonData 업로드할 JSON 데이터
     * @return Response<PinataResponse>
     */
    suspend fun uploadJsonToIpfs(apiKey: String, jsonData: Map<String, Any>): Response<PinataResponse> {
        try {
            // Pinata API 호출
            return PinataApiClient.pinataService.pinJSONToIPFS(apiKey, jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "JSON 업로드 중 오류 발생", e)
            throw e
        }
    }
    
    /**
     * Uri에서 임시 파일을 생성합니다.
     */
    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(context, uri)
            val tempFile = File.createTempFile("ipfs_upload_", "_$fileName", context.cacheDir)
            
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            return tempFile
        } catch (e: IOException) {
            Log.e(TAG, "임시 파일 생성 중 오류 발생", e)
            return null
        }
    }
    
    /**
     * Uri에서 파일 이름을 가져옵니다.
     */
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "unknown_file"
    }
} 