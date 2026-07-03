package com.tyshko.webvetcare.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.tyshko.auth.domain.usecase.RefreshTokenUseCase
import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.security.jwt.JwtProvider
import com.tyshko.webvetcare.feature.auth.login.LoginScreen
import com.tyshko.webvetcare.feature.auth.register.RegistrationScreen
import com.tyshko.webvetcare.feature.dashboard.UserDashboardScreen
import com.tyshko.webvetcare.feature.landing.LandingScreen
import org.koin.compose.koinInject

@Composable
fun NavigationController() {
    val tokenStorage: TokenStorage = koinInject()
    val jwtProvider: JwtProvider = koinInject()
    val refreshTokenUseCase: RefreshTokenUseCase = koinInject()

    val initialDestination: AppDestination = remember {
        val accessToken = tokenStorage.getAccessToken()
        when {
            accessToken != null && jwtProvider.validateToken(accessToken) != null ->
                AppDestination.Dashboard
            refreshTokenUseCase() != null ->
                AppDestination.Dashboard
            else -> AppDestination.Landing
        }
    }

    val backStack = rememberNavBackStack(initialDestination)

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
                    }
                )
            }
        }
    )
}

