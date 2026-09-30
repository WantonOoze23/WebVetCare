package com.tyshko.webvetcare.feature.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.usecase.RegisterUseCase
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.model.User
import com.tyshko.user.domain.repository.UserRepository
import com.tyshko.webvetcare.navigation.AppDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow(RegisterContract.State())
    val registerState : StateFlow<RegisterContract.State> = _registerState.asStateFlow()

    private val _effect = Channel<RegisterContract.Effect>()
    val effectFlow = _effect.receiveAsFlow()

    fun onEvent(event: RegisterContract.Event){
        when(event){
            is RegisterContract.Event.OnEmailChanged -> _registerState.update { it.copy(email = event.email) }
            is RegisterContract.Event.OnUserNameChanged -> _registerState.update { it.copy(userName = event.userName) }
            RegisterContract.Event.OnLoginClicked -> sendEffect(RegisterContract.Effect.Navigate(AppDestination.Login))
            is RegisterContract.Event.OnPasswordChanged -> _registerState.update { it.copy(password = event.password) }
            RegisterContract.Event.OnRegisterClicked -> register()
        }
    }

    private fun register() = viewModelScope.launch{
        val currentState = _registerState.value

        _registerState.update { it.copy(isLoading = true) }
        val result = registerUseCase(
            email = currentState.email,
            password = currentState.password
        )


        try {
            when (result) {
                is AuthResult.Error -> {
                    val message = when (result) {
                        is AuthResult.Error.UserAlreadyExists -> "An account with this email already exists."
                        is AuthResult.Error.Unknown -> "Registration failed. Please try again."
                        else -> "Registration failed."
                    }
                    sendEffect(RegisterContract.Effect.ShowSnackbar(message))
                }

                is AuthResult.Success -> {

                    val newUser = User(
                        id = result.userId,
                        userName = currentState.userName,
                        email = currentState.email,
                        roles = listOf(Role.User),
                        doctorProfile = null,
                        patientProfile = null
                    )

                    userRepository.saveUser(newUser)

                    _registerState.update { it.copy(isLoading = false) }

                    Log.d("Registration db", "Registration result: $newUser")

                    sendEffect(RegisterContract.Effect.Navigate(AppDestination.Dashboard))
                }
            }
        } finally {
            _registerState.update { it.copy(isLoading = false) }
        }
        Log.d("Registration", "Registration result: $result")
    }

    private fun sendEffect(effect: RegisterContract.Effect) = viewModelScope.launch{
        _effect.send(effect)
    }
}