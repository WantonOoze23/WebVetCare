package com.tyshko.webvetcare.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.webvetcare.firebase.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsContract.State())
    val state: StateFlow<SettingsContract.State> = _state

    private val _effect = Channel<SettingsContract.Effect>()
    val effectFlow = _effect.receiveAsFlow()

    init {
        loadCurrentValues()
        fetchRemoteConfig()
        listenForUpdates()
    }

    fun onEvent(event: SettingsContract.Event) {
        when (event) {
            SettingsContract.Event.LoadSettings -> loadCurrentValues()
            SettingsContract.Event.RefreshConfig -> fetchRemoteConfig()
        }
    }

    private fun loadCurrentValues() {
        _state.update {
            it.copy(
                testData = settingsRepository.getTestData(),
                landingColor = settingsRepository.getLandingColor(),
                welcomeMessage = settingsRepository.getWelcomeMessage(),
                isOnline = settingsRepository.isOnline()
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

    private fun listenForUpdates() {
        settingsRepository.getConfigUpdateListener {
            Log.d("SettingsViewModel", "Real-time config update received")
            loadCurrentValues()
            viewModelScope.launch {
                sendEffect(SettingsContract.Effect.ShowSnackbar("Configuration hot-reloaded"))
            }
        }
    }

    private suspend fun sendEffect(effect: SettingsContract.Effect) {
        _effect.send(effect)
    }
}
