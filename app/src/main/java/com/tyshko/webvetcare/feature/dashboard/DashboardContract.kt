package com.tyshko.webvetcare.feature.dashboard

import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.User

class DashboardContract {

    sealed class DashboardRoute(val title: String) {
        object UserScreen : DashboardRoute("My Profile")
        object BecomeDoctor : DashboardRoute("Become Doctor")
        object DoctorProfile : DashboardRoute("Doctor Data")
        object BecomePatient : DashboardRoute("Become Patient")
        object PatientProfile : DashboardRoute("Patient Data")
        object Settings : DashboardRoute("Settings")
    }

    data class State(
        val user: User? = null,
        val isMenuExpanded: Boolean = true,
        val isLoading: Boolean = false,
        val currentRoute: DashboardRoute = DashboardRoute.UserScreen
    )

    sealed class Event {
        object FetchUser : Event()
        object ToggleMenu : Event()
        object OnLogout : Event()
        data class ChangeRoute(val route: DashboardRoute) : Event()
        data class BecomeDoctor(val profile: DoctorProfile) : Event()
        data class BecomePatient(val profile: PatientProfile) : Event()
    }

    sealed class Effect {
        data class ShowSnackbar(val message: String) : Effect()
        object NavigateToLogin : Effect()
    }
}