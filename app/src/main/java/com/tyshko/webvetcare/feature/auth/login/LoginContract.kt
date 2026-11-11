package com.tyshko.webvetcare.feature.auth.login

class LoginContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
    )

    sealed class Event{
        data class OnEmailChanged(val email: String) : Event()
        data class OnPasswordChanged(val password: String) : Event()
        object OnLoginClicked : Event()
        object OnRegisterClicked : Event()
    }

    sealed class Effect{
        data class ShowSnackbar(val message: String) : Effect()
        object NavigateToDashboard : Effect()
        object NavigateToRegister : Effect()
    }
}