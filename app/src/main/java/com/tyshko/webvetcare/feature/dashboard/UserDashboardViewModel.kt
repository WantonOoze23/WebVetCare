package com.tyshko.webvetcare.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.security.jwt.JwtProvider
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserDashboardViewModel(
    private val userRepository: UserRepository,
    private val tokenStorage: TokenStorage,
    private val jwtProvider: JwtProvider
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
            is DashboardContract.Event.ChangeRoute -> _dashboardState.update { it.copy(currentRoute = event.route) }
            is DashboardContract.Event.BecomeDoctor -> becomeDoctor(event.profile)
            is DashboardContract.Event.BecomePatient -> becomePatient(event.profile)
        }
    }

    private fun fetchUser() = viewModelScope.launch {
        _dashboardState.update { it.copy(isLoading = true) }

        val token = tokenStorage.getAccessToken()

        val decodedJwt = token?.let { jwtProvider.validateToken(it) }
        val currentUserId = decodedJwt?.subject

        if (currentUserId != null) {
            val user = userRepository.getUser(currentUserId)
            Log.d("UserDashboardViewModel", "Fetched user: $user")
            _dashboardState.update { it.copy(user = user, isLoading = false) }
        } else {
            _dashboardState.update { it.copy(isLoading = false) }
            sendEffect(DashboardContract.Effect.ShowSnackbar("Session expired. Please log in again."))
            logout()
        }
    }

    private fun becomeDoctor(profile: DoctorProfile) = viewModelScope.launch {
        val user = _dashboardState.value.user ?: return@launch
        userRepository.saveDoctorProfile(user.id, profile)
        val updatedRoles = user.roles.toMutableList().apply { if (!contains(Role.Doctor)) add(
            Role.Doctor) }
        userRepository.saveUser(user.copy(roles = updatedRoles))

        sendEffect(DashboardContract.Effect.ShowSnackbar("Success! You are now a Doctor."))
        fetchUser()
        onEvent(DashboardContract.Event.ChangeRoute(DashboardContract.DashboardRoute.UserScreen))
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
        onEvent(DashboardContract.Event.ChangeRoute(DashboardContract.DashboardRoute.UserScreen))
    }

    private fun logout(){
        tokenStorage.clearTokens()
        viewModelScope.launch { _effect.send(DashboardContract.Effect.NavigateToLogin) }
    }

    private fun sendEffect(effect: DashboardContract.Effect) = viewModelScope.launch{
        _effect.send(effect)
    }
}