package com.tyshko.webvetcare.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tyshko.webvetcare.feature.auth.login.LoginScreen
import com.tyshko.webvetcare.feature.auth.register.RegistrationScreen
import com.tyshko.webvetcare.feature.dashboard.UserDashboardScreen
import com.tyshko.webvetcare.feature.landing.LandingScreen

@Composable
fun NavigationController(
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navHostController,
        startDestination = "landing",
        // Глобальні анімації переходів для всіх екранів згідно з ТЗ
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) }
    ) {

        composable("landing") {
            LandingScreen(
                navigationToLoginScreen = { navHostController.navigate("login") },
                navigationToRegisterScreen = { navHostController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateToDashboard = {
                    navHostController.navigate("dashboard") {
                        // Видаляємо екрани логіну та лендінгу з історії, щоб не можна було повернутися назад кнопкою "Back"
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navHostController.navigate("register") }
            )
        }

        composable("register") {
            RegistrationScreen(
                onNavigateToDashboard = {
                    navHostController.navigate("dashboard") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navHostController.navigate("login") }
            )
        }

        composable("dashboard") {
            UserDashboardScreen()
        }
    }
}