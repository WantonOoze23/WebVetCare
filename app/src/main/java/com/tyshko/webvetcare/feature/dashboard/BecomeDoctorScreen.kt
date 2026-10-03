package com.tyshko.webvetcare.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.webvetcare.navigation.AppDestination
import org.koin.androidx.compose.koinViewModel

@Composable
fun BecomeDoctorScreen(
    modifier: Modifier = Modifier,
    onNavigate: (AppDestination) -> Unit = {},
    viewModel: UserDashboardViewModel = koinViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is DashboardContract.Effect.Navigate -> onNavigate(effect.destination)
                else -> {} // Skip snackbars since screen will pop
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Become a Doctor", style = MaterialTheme.typography.titleLarge)

        var specialization by remember { mutableStateOf("") }
        var licenseNumber by remember { mutableStateOf("") }
        var clinicAddress by remember { mutableStateOf("") }
        var availability by remember { mutableStateOf("") }

        OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("License Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = clinicAddress, onValueChange = { clinicAddress = it }, label = { Text("Clinic Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("Availability") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                viewModel.onEvent(DashboardContract.Event.BecomeDoctor(
                    DoctorProfile(specialization, licenseNumber, clinicAddress, availability)
                ))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = specialization.isNotBlank() && licenseNumber.isNotBlank()
        ) {
            Text("Submit Doctor Application")
        }
    }
}
