package com.tyshko.webvetcare.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed class AppDestination : NavKey {
    @Serializable data object Landing : AppDestination()
    @Serializable data object Login : AppDestination()
    @Serializable data object Register : AppDestination()
    @Serializable data object Dashboard : AppDestination()
    @Serializable data object BecomeDoctor : AppDestination()
    @Serializable data object DoctorProfile : AppDestination()
    @Serializable data object BecomePatient : AppDestination()
    @Serializable data object PatientProfile : AppDestination()

}
