package com.tyshko.webvetcare.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.config.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private var listenerRegistration: AutoCloseable? = null

    private val _state = MutableStateFlow(SettingsContract.State())
    val state: StateFlow<SettingsContract.State> = _state

    private val _effect = Channel<SettingsContract.Effect>()
    val effectFlow = _effect.receiveAsFlow()

    fun startListening() {
        listenerRegistration = settingsRepository.addConfigUpdateListener { loadCurrentValues() }
    }
    override fun onCleared() {
        listenerRegistration?.close()
        super.onCleared()
    }

    fun onEvent(event: SettingsContract.Event) {
        when (event) {
            SettingsContract.Event.LoadSettings -> loadCurrentValues()
            SettingsContract.Event.RefreshConfig -> fetchRemoteConfig()
        }
    }

    private fun loadCurrentValues() {
        val config = settingsRepository.getConfig()
        _state.update {
            it.copy(
                testData = config.testData,
                appLanguage = config.appLanguage,
                landingColor = config.landingColor,
                welcomeMessage = config.welcomeMessage,
                isOnline = config.isOnline
            )
        }
    }

    private fun fetchRemoteConfig() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val activated = settingsRepository.fetchAndActivate()
                Log.d("SettingsViewModel", "fetchAndActivate activated new values: $activated")
                loadCurrentValues()
                if (activated) {
                    sendEffect(SettingsContract.Effect.ShowSnackbar("Configuration updated from Firebase"))
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to fetch remote config", e)
                sendEffect(SettingsContract.Effect.ShowSnackbar("Failed to fetch configuration"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun sendEffect(effect: SettingsContract.Effect) {
        _effect.send(effect)
    }
}
