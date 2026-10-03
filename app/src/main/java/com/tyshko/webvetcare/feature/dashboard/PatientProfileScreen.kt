package com.tyshko.webvetcare.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun PatientProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: UserDashboardViewModel = koinViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val profile = state.user?.patientProfile

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (profile == null) {
            Text("No patient profile found.")
        } else {
            Text("Patient Information", style = MaterialTheme.typography.titleLarge)
            ProfileInfoRow(label = "Phone", value = profile.contactPhoneNumber)
            ProfileInfoRow(label = "Email", value = profile.contactEmail)
        }
    }
}
