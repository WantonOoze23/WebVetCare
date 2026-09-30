package com.tyshko.webvetcare.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.tyshko.webvetcare.feature.auth.login.LoginScreen
import com.tyshko.webvetcare.feature.auth.register.RegistrationScreen
import com.tyshko.webvetcare.feature.dashboard.BecomeDoctorScreen
import com.tyshko.webvetcare.feature.dashboard.BecomePatientScreen
import com.tyshko.webvetcare.feature.dashboard.DoctorProfileScreen
import com.tyshko.webvetcare.feature.dashboard.PatientProfileScreen
import com.tyshko.webvetcare.feature.dashboard.UserDashboardScreen
import com.tyshko.webvetcare.feature.landing.LandingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationController() {
    val viewModel : SessionViewModel = koinViewModel()
    val sessionState by viewModel.sessionState.collectAsState()

    when (val state = sessionState) {
        is SessionState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is SessionState.Ready -> {
            val backStack = rememberNavBackStack(state.startDestination)

            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                popTransitionSpec = {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                },
                entryProvider = entryProvider {
                    entry<AppDestination.Landing> {
                        LandingScreen(
                            navigationToLoginScreen = { backStack.add(AppDestination.Login) },
                            navigationToRegisterScreen = { backStack.add(AppDestination.Register) }
                        )
                    }

                    entry<AppDestination.Login> {
                        LoginScreen(
                            onNavigateToDashboard = {
                                backStack.clear()
                                backStack.add(AppDestination.Dashboard)
                            },
                            onNavigateToRegister = { backStack.add(AppDestination.Register) }
                        )
                    }

                    entry<AppDestination.Register> {
                        RegistrationScreen(
                            onNavigateToDashboard = {
                                backStack.clear()
                                backStack.add(AppDestination.Dashboard)
                            },
                            onNavigateToLogin = { backStack.add(AppDestination.Login) }
                        )
                    }

                    entry<AppDestination.Dashboard> {
                        UserDashboardScreen(
                            onNavigateToLogin = {
                                backStack.clear()
                                backStack.add(AppDestination.Login)
                            },
                            onNavigate = { destination -> backStack.add(destination) }
                        )
                    }
                    entry<AppDestination.BecomeDoctor> {
                        BecomeDoctorScreen()
                    }
                    entry<AppDestination.DoctorProfile> {
                        DoctorProfileScreen()
                    }
                    entry<AppDestination.BecomePatient> {
                        BecomePatientScreen()
                    }
                    entry<AppDestination.PatientProfile> {
                        PatientProfileScreen()
                    }
                }
            )
        }
    }
}

