package com.ham.tools.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ham.tools.data.remote.qrz.QrzLogbookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QrzVerifyUi {
    data object Idle : QrzVerifyUi
    data object Loading : QrzVerifyUi
    data class Success(val summary: String) : QrzVerifyUi
    data class Error(val message: String) : QrzVerifyUi
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val qrzLogbookRepository: QrzLogbookRepository
) : ViewModel() {

    private val _verifyUi = MutableStateFlow<QrzVerifyUi>(QrzVerifyUi.Idle)
    val verifyUi: StateFlow<QrzVerifyUi> = _verifyUi.asStateFlow()

    fun verifyQrzKey(apiKey: String, callsignForUa: String) {
        val key = apiKey.trim()
        if (key.isEmpty()) {
            _verifyUi.value = QrzVerifyUi.Error("empty_key")
            return
        }
        viewModelScope.launch {
            _verifyUi.value = QrzVerifyUi.Loading
            _verifyUi.value = qrzLogbookRepository.verifyKey(key, callsignForUa).fold(
                onSuccess = { QrzVerifyUi.Success(it) },
                onFailure = { e ->
                    QrzVerifyUi.Error(e.message ?: e::class.simpleName ?: "error")
                }
            )
        }
    }

    fun clearVerifyUi() {
        _verifyUi.value = QrzVerifyUi.Idle
    }
}
