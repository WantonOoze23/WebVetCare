package com.tyshko.webvetcare.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.domain.usecase.RefreshTokenUseCase
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.jwt.TokenValidationResult
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.repository.UserRepository
import com.tyshko.webvetcare.navigation.AppDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserDashboardViewModel(
    private val userRepository: UserRepository,
    private val tokenStorage: TokenStorage,
    private val jwtProvider: JwtProviderContract,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardContract.State())
    val dashboardState : StateFlow<DashboardContract.State> = _dashboardState

    private val _effect = Channel<DashboardContract.Effect>()
    val effectFlow = _effect.receiveAsFlow()

    init{
        onEvent(DashboardContract.Event.FetchUser)
    }

    fun onEvent(event: DashboardContract.Event) {
        when (event) {
            DashboardContract.Event.FetchUser -> fetchUser()
            DashboardContract.Event.OnLogout -> logout()
            DashboardContract.Event.ToggleMenu -> _dashboardState.update { it.copy(isMenuExpanded = !it.isMenuExpanded) }
            is DashboardContract.Event.BecomeDoctor -> becomeDoctor(event.profile)
            is DashboardContract.Event.BecomePatient -> becomePatient(event.profile)
        }
    }

    private fun fetchUser() = viewModelScope.launch {
        _dashboardState.update { it.copy(isLoading = true) }


        try{
            val token = tokenStorage.getAccessToken()

            val currentUserId : String? = when(val validation = token?.let { jwtProvider.validateToken(it) }) {
                is TokenValidationResult.Valid -> validation.decodedJWT.subject
                is TokenValidationResult.Expired -> null
                is TokenValidationResult.InvalidSignature -> {
                    Log.w("UserDashboardViewModel", "Security: invalid token signature detected")
                    forceLogout()
                    return@launch
                }
                is TokenValidationResult.MalformedToken -> {
                    forceLogout()
                    return@launch
                }
                null -> null
            }

            if (currentUserId != null){
                val user = userRepository.getUser(currentUserId)
                Log.d("UserDashboardViewModel", "Fetched user: $user")
                _dashboardState.update { it.copy(user = user, isLoading = false) }
            } else {
                Log.d("UserDashboardViewModel", "Access token expired, attempting refresh...")
                val newAccessToken = refreshTokenUseCase()

                if (newAccessToken != null) {
                    val refreshedUserId = when(val refreshUser = jwtProvider.validateToken(newAccessToken)){
                        is TokenValidationResult.Valid -> refreshUser.decodedJWT.subject
                        else -> null
                    }

                    if (refreshedUserId != null) {
                        val user = userRepository.getUser(refreshedUserId)
                        Log.d("UserDashboardViewModel", "Token refreshed, fetched user: $user")
                        _dashboardState.update { it.copy(user = user, isLoading = false) }
                        sendEffect(DashboardContract.Effect.ShowSnackbar("Session renewed automatically."))
                    } else {
                        forceLogout()
                    }
                } else {
                    Log.d("UserDashboardViewModel", "Refresh token expired, forcing logout.")
                    forceLogout()
                }
            }

        } finally {
            _dashboardState.update { if (it.isLoading) it.copy(isLoading = false) else it }
        }
    }

    private fun forceLogout() {
        _dashboardState.update { it.copy(isLoading = false) }
        sendEffect(DashboardContract.Effect.ShowSnackbar("Session expired. Please log in again."))
        logout()
    }

    private fun becomeDoctor(profile: DoctorProfile) = viewModelScope.launch {
        val user = _dashboardState.value.user ?: return@launch
        userRepository.saveDoctorProfile(user.id, profile)
        val updatedRoles = user.roles.toMutableList().apply { if (!contains(Role.Doctor)) add(
            Role.Doctor) }
        userRepository.saveUser(user.copy(roles = updatedRoles))

        sendEffect(DashboardContract.Effect.ShowSnackbar("Success! You are now a Doctor."))
        fetchUser()
        sendEffect(DashboardContract.Effect.Navigate(AppDestination.Dashboard))
    }

    private fun becomePatient(profile: PatientProfile) = viewModelScope.launch {
        val user = _dashboardState.value.user ?: return@launch
        userRepository.savePatientProfile(user.id, profile)

        val updatedRoles = user.roles.toMutableList().apply {
            if (!contains(Role.Patient)) add(Role.Patient)
        }
        userRepository.saveUser(user.copy(roles = updatedRoles))

        sendEffect(DashboardContract.Effect.ShowSnackbar("Success! You are now a Patient."))
        fetchUser()
        sendEffect(DashboardContract.Effect.Navigate(AppDestination.Dashboard))
    }

    private fun logout(){
        tokenStorage.clearTokens()
        viewModelScope.launch { _effect.send(DashboardContract.Effect.NavigateToLogin) }
    }

    private fun sendEffect(effect: DashboardContract.Effect) = viewModelScope.launch{
        _effect.send(effect)
    }
}