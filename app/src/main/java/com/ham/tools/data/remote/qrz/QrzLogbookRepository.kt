package com.ham.tools.data.remote.qrz

import com.ham.tools.BuildConfig
import com.ham.tools.data.model.AppSettings
import com.ham.tools.data.model.QsoLog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * QRZ Logbook：STATUS 校验与 INSERT 上传。
 */
@Singleton
class QrzLogbookRepository @Inject constructor(
    private val api: QrzLogbookApi
) {

    fun buildUserAgent(callsignForIdentification: String): String {
        val cs = callsignForIdentification.ifBlank { "NOCALL" }.uppercase()
        val raw = "HamTools/${BuildConfig.VERSION_NAME} ($cs)"
        return raw.take(128)
    }

    suspend fun verifyKey(apiKey: String, callsignForUa: String): Result<String> {
        val key = apiKey.trim()
        if (key.isEmpty()) return Result.failure(IllegalArgumentException("empty_key"))

        return runCatching {
            val body = api.post(
                userAgent = buildUserAgent(callsignForUa),
                fields = mapOf(
                    "KEY" to key,
                    "ACTION" to "STATUS"
                )
            ).string()
            val map = QrzLogbookResponseParser.parse(body)
            val r = map.toQrzLogbookResult()
            when (r.resultCode) {
                "OK" -> map["DATA"] ?: "OK"
                "AUTH" -> throw QrzApiException(
                    map["REASON"] ?: "AUTH",
                    r.resultCode
                )
                "FAIL" -> throw QrzApiException(
                    map["REASON"] ?: "FAIL",
                    r.resultCode
                )
                else -> throw QrzApiException(
                    map["REASON"] ?: body.take(200),
                    r.resultCode.ifEmpty { "UNKNOWN" }
                )
            }
        }
    }

    /**
     * 上传一条 QSO；成功时返回 QRZ 返回的 LOGID（若有）。
     */
    suspend fun insertQso(
        settings: AppSettings,
        stationCallsign: String,
        log: QsoLog
    ): Result<Long?> {
        val key = settings.qrzLogbookApiKey.trim()
        if (key.isEmpty()) return Result.failure(IllegalStateException("no_qrz_key"))

        val adif = QrzAdifBuilder.buildInsertAdif(log, stationCallsign)
        val fields = mutableMapOf(
            "KEY" to key,
            "ACTION" to "INSERT",
            "ADIF" to adif
        )
        if (settings.qrzInsertReplaceDuplicates) {
            fields["OPTION"] = "REPLACE"
        }

        return runCatching {
            val body = api.post(
                userAgent = buildUserAgent(stationCallsign),
                fields = fields
            ).string()
            val map = QrzLogbookResponseParser.parse(body)
            val r = map.toQrzLogbookResult()
            when (r.resultCode) {
                "OK", "REPLACE" -> r.logId
                "AUTH" -> throw QrzApiException(
                    map["REASON"] ?: "AUTH",
                    r.resultCode
                )
                "FAIL" -> throw QrzApiException(
                    map["REASON"] ?: "FAIL",
                    r.resultCode
                )
                else -> throw QrzApiException(
                    map["REASON"] ?: body.take(200),
                    r.resultCode.ifEmpty { "UNKNOWN" }
                )
            }
        }
    }
}

class QrzApiException(
    message: String,
    val code: String
) : Exception(message)
