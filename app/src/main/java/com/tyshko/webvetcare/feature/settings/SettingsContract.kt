package com.tyshko.webvetcare.feature.settings

class SettingsContract {

    data class State(
        val testData: String = "",
        val landingColor: String = "",
        val welcomeMessage: String = "",
        val isOnline: Boolean = false,
        val isLoading: Boolean = false
    )

    sealed class Event {
        object LoadSettings : Event()
        object RefreshConfig : Event()
    }

    sealed class Effect {
        data class ShowSnackbar(val message: String) : Effect()
    }
}
