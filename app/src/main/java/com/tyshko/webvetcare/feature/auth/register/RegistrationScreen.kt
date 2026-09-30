package com.tyshko.webvetcare.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.tyshko.webvetcare.ui.theme.Padding
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is RegisterContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is RegisterContract.Effect.NavigateToDashboard -> onNavigateToDashboard()
                is RegisterContract.Effect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Padding.extra),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create an Account",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(Padding.extra))

            OutlinedTextField(
                value = state.userName,
                onValueChange = { viewModel.onEvent(RegisterContract.Event.OnUserNameChanged(it)) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Padding.huge))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(RegisterContract.Event.OnEmailChanged(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Padding.huge))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onEvent(RegisterContract.Event.OnPasswordChanged(it)) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Padding.extra))

            Button(
                onClick = { viewModel.onEvent(RegisterContract.Event.OnRegisterClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(Padding.small))
                } else {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(Padding.huge))

            TextButton(onClick = { viewModel.onEvent(RegisterContract.Event.OnLoginClicked) }) {
                Text("Already have an account? Login")
            }
        }
    }
}