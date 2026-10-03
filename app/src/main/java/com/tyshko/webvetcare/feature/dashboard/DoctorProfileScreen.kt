package com.tyshko.webvetcare.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun DoctorProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: UserDashboardViewModel = koinViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val profile = state.user?.doctorProfile

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (profile == null) {
            Text("No doctor profile found.")
        } else {
            Text("Doctor Information", style = MaterialTheme.typography.titleLarge)
            ProfileInfoRow(label = "Specialization", value = profile.specialization)
            ProfileInfoRow(label = "License", value = profile.licenseNumber)
            ProfileInfoRow(label = "Clinic", value = profile.clinicAddress)
            ProfileInfoRow(label = "Availability", value = profile.availability)
        }
    }
}
