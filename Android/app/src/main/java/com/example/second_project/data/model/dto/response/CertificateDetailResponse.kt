package com.example.second_project.data.model.dto.response

import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

data class CertificateDetailResponse(
    @SerializedName("timeStamp")
    val timeStamp: String? = null,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("data")
    val data: CertificateDetailData? = null
) {
    companion object {
        val typeAdapter = object : TypeAdapter<CertificateDetailResponse>() {
            override fun write(out: JsonWriter, value: CertificateDetailResponse?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                out.beginObject()
                out.name("timeStamp").value(value.timeStamp)
                out.name("code").value(value.code)
                out.name("status").value(value.status)
                out.name("data").value(Gson().toJson(value.data))
                out.endObject()
            }

            override fun read(`in`: JsonReader): CertificateDetailResponse {
                var timeStamp: String? = null
                var code: Int? = null
                var status: String? = null
                var data: CertificateDetailData? = null

                `in`.beginObject()
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "timeStamp" -> timeStamp = `in`.nextString()
                        "code" -> code = `in`.nextInt()
                        "status" -> status = `in`.nextString()
                        "data" -> {
                            if (`in`.peek() == JsonToken.NULL) {
                                `in`.nextNull()
                                data = null
                            } else {
                                data = Gson().fromJson(`in`, CertificateDetailData::class.java)
                            }
                        }
                    }
                }
                `in`.endObject()
                return CertificateDetailResponse(timeStamp, code, status, data)
            }
        }
    }
}

data class CertificateDetailData(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("teacherName")
    val teacherName: String? = null,
    @SerializedName("teacherWallet")
    val teacherWallet: String? = null,
    @SerializedName("certificateDate")
    val certificateDate: String? = null,
    @SerializedName("certificate")
    val certificate: Int? = null,
    @SerializedName("qrCode")
    val qrCode: String? = null
) {
    companion object {
        val typeAdapter = object : TypeAdapter<CertificateDetailData>() {
            override fun write(out: JsonWriter, value: CertificateDetailData?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                out.beginObject()
                out.name("title").value(value.title)
                out.name("teacherName").value(value.teacherName)
                out.name("teacherWallet").value(value.teacherWallet)
                out.name("certificateDate").value(value.certificateDate)
                out.name("certificate").value(value.certificate)
                out.name("qrCode").value(value.qrCode)
                out.endObject()
            }

            override fun read(`in`: JsonReader): CertificateDetailData {
                var title: String? = null
                var teacherName: String? = null
                var teacherWallet: String? = null
                var certificateDate: String? = null
                var certificate: Int? = null
                var qrCode: String? = null

                `in`.beginObject()
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "title" -> title = `in`.nextString()
                        "teacherName" -> teacherName = `in`.nextString()
                        "teacherWallet" -> teacherWallet = `in`.nextString()
                        "certificateDate" -> certificateDate = `in`.nextString()
                        "certificate" -> certificate = `in`.nextInt()
                        "qrCode" -> {
                            if (`in`.peek() == JsonToken.NULL) {
                                `in`.nextNull()
                                qrCode = null
                            } else {
                                qrCode = `in`.nextString()
                            }
                        }
                    }
                }
                `in`.endObject()
                return CertificateDetailData(title, teacherName, teacherWallet, certificateDate, certificate, qrCode)
            }
        }
    }
}