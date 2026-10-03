package com.tyshko.webvetcare.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.webvetcare.navigation.AppDestination
import org.koin.androidx.compose.koinViewModel

@Composable
fun BecomePatientScreen(
    modifier: Modifier = Modifier,
    onNavigate: (AppDestination) -> Unit = {},
    viewModel: UserDashboardViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is DashboardContract.Effect.Navigate -> onNavigate(effect.destination)
                else -> {}
            }
        }
    }

    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Become a Patient", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                viewModel.onEvent(DashboardContract.Event.BecomePatient(PatientProfile(phone, email)))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = phone.isNotBlank() && email.isNotBlank()
        ) {
            Text("Submit Patient Application")
        }
    }
}
