package com.tyshko.webvetcare.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed class AppDestination : NavKey {
    @Serializable data object Landing : AppDestination()
    @Serializable data object Login : AppDestination()
    @Serializable data object Register : AppDestination()
    @Serializable data object Dashboard : AppDestination()
}
