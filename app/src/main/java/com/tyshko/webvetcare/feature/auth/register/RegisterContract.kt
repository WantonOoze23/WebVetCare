package com.tyshko.webvetcare.feature.auth.register

import com.tyshko.webvetcare.navigation.AppDestination

class RegisterContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val userName: String = "",
        val isLoading: Boolean = false
    )

    sealed class Event {
        data class OnEmailChanged(val email: String) : Event()
        data class OnPasswordChanged(val password: String) : Event()
        data class OnUserNameChanged(val userName: String) : Event()
        object OnRegisterClicked : Event()
        object OnLoginClicked : Event()
    }

    sealed class Effect {
        data class ShowSnackbar(val message: String) : Effect()
        data class Navigate(val destination: AppDestination) : Effect()
    }
}