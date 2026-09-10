package com.tyshko.webvetcare.feature.dashboard

import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.User
import com.tyshko.webvetcare.navigation.AppDestination

class DashboardContract {
    data class State(
        val user: User? = null,
        val isMenuExpanded: Boolean = true,
        val isLoading: Boolean = false,
    )

    sealed class Event {
        object FetchUser : Event()
        object ToggleMenu : Event()
        object OnLogout : Event()
        data class BecomeDoctor(val profile: DoctorProfile) : Event()
        data class BecomePatient(val profile: PatientProfile) : Event()
    }

    sealed class Effect {
        data class ShowSnackbar(val message: String) : Effect()
        object NavigateToLogin : Effect()
        data class Navigate(val destination: AppDestination) : Effect()
    }
}