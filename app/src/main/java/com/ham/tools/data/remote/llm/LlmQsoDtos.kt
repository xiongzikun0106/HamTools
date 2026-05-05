package com.ham.tools.data.remote.llm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LlmChatRequest(
    val model: String,
    val messages: List<LlmChatMessage>,
    @SerialName("temperature") val temperature: Double = 0.2,
    @SerialName("response_format") val responseFormat: LlmResponseFormat? = LlmResponseFormat()
)

@Serializable
data class LlmResponseFormat(
    @SerialName("type") val type: String = "json_object"
)

@Serializable
data class LlmChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class LlmChatResponse(
    val choices: List<LlmChoice> = emptyList()
)

@Serializable
data class LlmChoice(
    val message: LlmChoiceMessage? = null
)

@Serializable
data class LlmChoiceMessage(
    val content: String? = null
)

@Serializable
data class LlmParsedQsoFile(
    val qsos: List<LlmParsedQso> = emptyList()
)

@Serializable
data class LlmParsedQso(
    val callsign: String? = null,
    val frequency: String? = null,
    val mode: String? = null,
    val rstSent: String? = null,
    val rstRcvd: String? = null,
    val opName: String? = null,
    val qth: String? = null,
    val gridLocator: String? = null,
    val qslInfo: String? = null,
    val txPower: String? = null,
    val rig: String? = null,
    val myGridLocator: String? = null,
    val propagation: String? = null,
    val qslStatus: String? = null,
    val remarks: String? = null
)
