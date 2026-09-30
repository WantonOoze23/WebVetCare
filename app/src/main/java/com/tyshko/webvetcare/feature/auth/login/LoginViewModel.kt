package com.tyshko.webvetcare.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.usecase.LoginUseCase
import com.tyshko.webvetcare.navigation.AppDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginContract.State())
    val loginState : StateFlow<LoginContract.State> = _loginState.asStateFlow()

    private val _effect = Channel<LoginContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: LoginContract.Event){
        when(event) {
            is LoginContract.Event.OnEmailChanged -> _loginState.update { it.copy(email = event.email) }
            LoginContract.Event.OnLoginClicked -> login()
            is LoginContract.Event.OnPasswordChanged -> _loginState.update { it.copy(password = event.password) }
            LoginContract.Event.OnRegisterClicked -> sendEffect(LoginContract.Effect.Navigate(AppDestination.Register))
        }
    }

    private fun login() = viewModelScope.launch{
        _loginState.update { it.copy(isLoading = true) }
        val result = loginUseCase(
            email = _loginState.value.email,
            password = _loginState.value.password
        )
        _loginState.update { it.copy(isLoading = false) }

        try {
            when (result) {
                is AuthResult.Error -> {
                    val message = when (result) {
                        is AuthResult.Error.UserNotFound -> "Incorrect credentials."
                        is AuthResult.Error.WrongPassword -> "Incorrect credentials."
                        is AuthResult.Error.UserDataCorrupted -> "Account error. Please contact support."
                        is AuthResult.Error.Unknown -> "Login failed. Please try again."
                        else -> "Login failed."
                    }
                    sendEffect(LoginContract.Effect.ShowSnackbar(message))
                }

                is AuthResult.Success -> sendEffect(LoginContract.Effect.Navigate(AppDestination.Dashboard))
            }
        } finally {
            _loginState.update { it.copy(isLoading = false) }
        }
    }

    private fun sendEffect(effect: LoginContract.Effect) = viewModelScope.launch {
        _effect.send(effect)
    }

}