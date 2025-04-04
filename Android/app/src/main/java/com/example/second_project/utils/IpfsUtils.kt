package com.example.second_project.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.second_project.network.PinataApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
     * IPFS에서 파일을 다운로드합니다.
     * @param context 컨텍스트
     * @param cid IPFS CID (Content Identifier)
     * @param fileName 다운로드할 파일 이름 (확장자 포함)
     * @return 다운로드된 파일의 Uri 또는 null (실패 시)
     */
    suspend fun downloadFileFromIpfs(context: Context, cid: String, fileName: String): Uri? {
        try {
            // 다운로드 URL 생성 (Pinata Gateway 사용)
            val ipfsUrl = "https://gateway.pinata.cloud/ipfs/$cid"
            Log.d(TAG, "다운로드 URL: $ipfsUrl")
            
            // 다운로드 디렉토리 생성
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "LectureMaterials")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            // 파일 이름에 타임스탬프 추가
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val finalFileName = "${fileName.substringBeforeLast(".")}_$timestamp.${fileName.substringAfterLast(".")}"
            val file = File(downloadDir, finalFileName)
            
            // 파일 다운로드
            URL(ipfsUrl).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d(TAG, "파일 다운로드 성공: ${file.absolutePath}")
            return Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "파일 다운로드 중 오류 발생", e)
            return null
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