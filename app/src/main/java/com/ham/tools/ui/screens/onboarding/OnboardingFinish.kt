package com.ham.tools.ui.screens.onboarding

/**
 * 引导完成时回传给 [com.ham.tools.ui.MainViewModel] 的数据。
 */
data class OnboardingFinish(
    val callsign: String,
    /** 非空表示保存 QRZ Logbook API Key */
    val qrzApiKey: String? = null,
    val qrzAutoSync: Boolean = false,
    val qrzInsertReplaceDuplicates: Boolean = false
)
