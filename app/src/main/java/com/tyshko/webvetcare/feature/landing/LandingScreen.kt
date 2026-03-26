package com.tyshko.webvetcare.feature.landing

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    navigationToLoginScreen: () -> Unit,
    navigationToRegisterScreen: () -> Unit,
) {
    Text(text = "WebVetCare")
    Text(text = "Your online veterinary care platform")
    Row {
        Button(
            onClick = TODO()
        ) { }
        Button(
            onClick = TODO()
        ) { }
    }
}