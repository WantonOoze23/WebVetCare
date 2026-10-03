package com.tyshko.webvetcare.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.domain.usecase.RefreshTokenUseCase
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.jwt.TokenValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionViewModel(
    private val tokenStorage: TokenStorage,
    private val jwtProvider: JwtProviderContract,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : ViewModel(){

    private val _sessionState : MutableStateFlow<SessionState> = MutableStateFlow(SessionState.Loading)
    val sessionState : StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        checkSession()
    }
    private fun checkSession() = viewModelScope.launch {
        val destination = withContext(Dispatchers.IO) {
            val accessToken = tokenStorage.getAccessToken()

            val isAccessValid = accessToken != null && jwtProvider.validateToken(accessToken) is TokenValidationResult.Valid

            when {
                isAccessValid -> AppDestination.Dashboard
                refreshTokenUseCase() != null -> AppDestination.Dashboard
                else -> AppDestination.Landing
            }
        }
        _sessionState.value = SessionState.Ready(destination)
    }

}


sealed class SessionState {
    object Loading : SessionState()
    data class Ready(val startDestination: AppDestination) : SessionState()
}