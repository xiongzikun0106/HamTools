package com.ham.tools.data.remote.qrz

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * 解析 QRZ API 返回的 `KEY=VALUE&...`（URL 编码）字符串。
 */
object QrzLogbookResponseParser {

    fun parse(body: String): Map<String, String> {
        val out = linkedMapOf<String, String>()
        if (body.isBlank()) return out
        body.split('&').forEach { part ->
            val idx = part.indexOf('=')
            if (idx < 0) return@forEach
            val k = urlDecode(part.substring(0, idx))
            val v = urlDecode(part.substring(idx + 1))
            out[k] = v
        }
        return out
    }

    private fun urlDecode(s: String): String =
        URLDecoder.decode(s, StandardCharsets.UTF_8.name())
}

data class QrzLogbookResult(
    val resultCode: String,
    val reason: String?,
    val logId: Long?,
    val count: Int?
)

fun Map<String, String>.toQrzLogbookResult(): QrzLogbookResult {
    val result = this["RESULT"] ?: ""
    val reason = this["REASON"]
    val logId = this["LOGID"]?.toLongOrNull()
    val count = this["COUNT"]?.toIntOrNull()
    return QrzLogbookResult(
        resultCode = result,
        reason = reason,
        logId = logId,
        count = count
    )
}
