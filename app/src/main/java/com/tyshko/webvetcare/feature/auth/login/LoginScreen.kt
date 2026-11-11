package com.tyshko.webvetcare.feature.auth.login

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
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Обробка ефектів (навігація та повідомлення)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is LoginContract.Effect.NavigateToDashboard -> onNavigateToDashboard()
                is LoginContract.Effect.NavigateToRegister -> onNavigateToRegister()
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
                .padding(Padding.extra), // Використовуємо ваш Padding
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign in to WebVetCare",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(Padding.extra))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(LoginContract.Event.OnEmailChanged(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Padding.huge))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onEvent(LoginContract.Event.OnPasswordChanged(it)) },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Padding.extra))

            Button(
                onClick = { viewModel.onEvent(LoginContract.Event.OnLoginClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(Padding.small))
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(Padding.huge))

            TextButton(onClick = { viewModel.onEvent(LoginContract.Event.OnRegisterClicked) }) {
                Text("Don't have an account? Register")
            }
        }
    }
}