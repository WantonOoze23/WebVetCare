package com.tyshko.webvetcare.feature.landing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.tyshko.webvetcare.firebase.domain.repository.SettingsRepository
import com.tyshko.webvetcare.ui.theme.Padding
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    navigationToLoginScreen: () -> Unit,
    navigationToRegisterScreen: () -> Unit,
    settingsRepository: SettingsRepository = koinInject()
) {
    var welcomeMessage by remember { mutableStateOf(settingsRepository.getWelcomeMessage()) }
    var particleColorHex by remember { mutableStateOf(settingsRepository.getLandingColor()) }

    LaunchedEffect(Unit) {
        settingsRepository.getConfigUpdateListener {
            welcomeMessage = settingsRepository.getWelcomeMessage()
            particleColorHex = settingsRepository.getLandingColor()
        }
    }
    val particleColor = remember(particleColorHex) {
        try {
            Color(android.graphics.Color.parseColor(particleColorHex))
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FloatingParticles(particleColor = particleColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Padding.extra),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WebVetCare",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Padding.huge))

            Text(
                text = welcomeMessage.ifEmpty { "Your online veterinary care platform" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(Padding.extra))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Padding.huge)
            ) {
                Button(onClick = navigationToLoginScreen) {
                    Text("Login")
                }
                OutlinedButton(onClick = navigationToRegisterScreen) {
                    Text("Register")
                }
            }
        }
    }
}

@Composable
fun FloatingParticles(particleColor: Color? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "particle_progress"
    )

    val particles = remember { List(30) {
        Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 10f + 5f)
    } }
    val fallbackColor = MaterialTheme.colorScheme.primary
    val color = particleColor ?: fallbackColor

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (xRatio, phaseOffset, radius) ->
            val yOffset = 1f - ((progress + phaseOffset) % 1f)
            val alpha = if (yOffset > 0.8f) (1f - yOffset) * 5f else yOffset

            drawCircle(
                color = color.copy(alpha = alpha * 0.3f), // Полупрозрачные частицы
                radius = radius,
                center = Offset(x = xRatio * size.width, y = yOffset * size.height)
            )
        }
    }
}