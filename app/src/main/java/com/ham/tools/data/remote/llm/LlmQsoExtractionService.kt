package com.ham.tools.data.remote.llm

import com.ham.tools.data.model.QsoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmQsoExtractionService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val mediaJson = "application/json; charset=utf-8".toMediaType()

    suspend fun extractQsos(
        endpointBase: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userMessage: String
    ): Result<List<QsoLog>> = withContext(Dispatchers.IO) {
        runCatching {
            val base = endpointBase.trimEnd('/')
            val url = "$base/chat/completions"
            val body = LlmChatRequest(
                model = model,
                messages = listOf(
                    LlmChatMessage(role = "system", content = systemPrompt),
                    LlmChatMessage(role = "user", content = userMessage)
                ),
                temperature = 0.2,
                responseFormat = LlmResponseFormat()
            )
            val reqJson = json.encodeToString(LlmChatRequest.serializer(), body)
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(reqJson.toRequestBody(mediaJson))
                .build()
            okHttpClient.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                require(resp.isSuccessful) { "HTTP ${resp.code}: $raw" }
                val parsed = json.decodeFromString(LlmChatResponse.serializer(), raw)
                val content = parsed.choices.firstOrNull()?.message?.content
                    ?: error("empty choices")
                val payload = extractJsonObject(content)
                val file = json.decodeFromString(LlmParsedQsoFile.serializer(), payload)
                val now = System.currentTimeMillis()
                LlmQsoMapper.toQsoLogs(file, now)
            }
        }
    }

    private fun extractJsonObject(text: String): String {
        val t = text.trim()
        val fence = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        fence.find(t)?.groupValues?.getOrNull(1)?.trim()?.let { return it }
        return t
    }
}
