package com.ham.tools.ui.screens.logbook

enum class VoiceQsoPhase {
    HIDDEN,
    PREPARING_MODEL,
    RECORDING,
    TRANSCRIBING,
    LLM_PARSING,
    ERROR
}

data class VoiceQsoUiState(
    val phase: VoiceQsoPhase = VoiceQsoPhase.HIDDEN,
    val asrPreview: String = "",
    val modelDownloadProgress: Float? = null,
    val statusLine: String = "",
    val errorMessage: String? = null
)
