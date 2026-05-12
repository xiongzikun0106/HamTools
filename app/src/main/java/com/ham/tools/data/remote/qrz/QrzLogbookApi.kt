package com.ham.tools.data.remote.qrz

import okhttp3.ResponseBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * QRZ Logbook API（application/x-www-form-urlencoded）
 *
 * @see [QRZ Logbook API](https://www.qrz.com/docs/logbook/QRZLogbookAPI.html)
 */
interface QrzLogbookApi {

    @FormUrlEncoded
    @POST("api")
    suspend fun post(
        @Header("User-Agent") userAgent: String,
        @FieldMap fields: Map<String, String>
    ): ResponseBody
}
