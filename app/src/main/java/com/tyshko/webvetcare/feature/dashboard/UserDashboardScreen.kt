package com.tyshko.webvetcare.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.model.User
import com.tyshko.webvetcare.navigation.AppDestination
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserDashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    onNavigate: (AppDestination) -> Unit = {},
    viewModel: UserDashboardViewModel = koinViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is DashboardContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is DashboardContract.Effect.NavigateToLogin -> onNavigateToLogin()
                is DashboardContract.Effect.Navigate -> onNavigate(effect.destination)
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Боковое меню (Side Navigation Menu)
            SideNavigationMenu(
                isExpanded = state.isMenuExpanded,
                userRoles = state.user?.roles ?: emptyList(),
                onToggleMenu = { viewModel.onEvent(DashboardContract.Event.ToggleMenu) },
                onLogout = { viewModel.onEvent(DashboardContract.Event.OnLogout) },
                onNavigate = { onNavigate(it) }
            )

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Имя пользователя)
                Text(
                    text = if (state.isLoading) "Loading..." else "Welcome, ${state.user?.userName ?: "Guest"}",
                    style = MaterialTheme.typography.headlineMedium
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                UserProfileContent(
                    user = state.user,
                    onSync = { viewModel.onEvent(DashboardContract.Event.FetchUser) }
                )
            }
        }
    }
}

@Composable
fun SideNavigationMenu(
    isExpanded: Boolean,
    userRoles: List<Role>,
    onNavigate: (AppDestination) -> Unit,
    onToggleMenu: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(if (isExpanded) 240.dp else 70.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalAlignment = if (isExpanded) Alignment.Start else Alignment.CenterHorizontally
    ) {
        // Кнопка переключения меню
        IconButton(
            onClick = onToggleMenu,
            modifier = Modifier.align(if (isExpanded) Alignment.End else Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Toggle Menu"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Группа: User
        MenuDivider(title = "USER", isExpanded = isExpanded)
        MenuItem(
            icon = Icons.Default.Person,
            text = "My Profile",
            isSelected = false,
            isExpanded = isExpanded,
            onClick = { onNavigate(AppDestination.Dashboard) }
        )

        // Группа: Patient
        MenuDivider(title = "PATIENT", isExpanded = isExpanded)
        if (userRoles.contains(Role.Patient)) {
            MenuItem(
                icon = Icons.Default.Favorite,
                text = "Patient Data",
                isSelected = false,
                isExpanded = isExpanded,
                onClick = { onNavigate(AppDestination.PatientProfile) }
            )
        } else {
            MenuItem(
                icon = Icons.Default.Add,
                text = "Become Patient",
                isSelected = false,
                isExpanded = isExpanded,
                onClick = { onNavigate(AppDestination.BecomePatient) }
            )
        }

        // Группа: Doctor
        MenuDivider(title = "DOCTOR", isExpanded = isExpanded)
        if (userRoles.contains(Role.Doctor)) {
            MenuItem(
                icon = Icons.Default.AccountBox,
                text = "Doctor Data",
                isSelected = false,
                isExpanded = isExpanded,
                onClick = { onNavigate(AppDestination.DoctorProfile) }
            )
        } else {
            MenuItem(
                icon = Icons.Default.Build,
                text = "Become Doctor",
                isSelected = false,
                isExpanded = isExpanded,
                onClick = { onNavigate(AppDestination.BecomeDoctor) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка логаута
        MenuItem(
            icon = Icons.Default.ExitToApp,
            text = "Logout",
            isSelected = false,
            isExpanded = isExpanded,
            onClick = onLogout
        )
    }
}

@Composable
fun UserProfileContent(user: User?, onSync: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileInfoRow(label = "User ID", value = user?.id ?: "N/A")
        ProfileInfoRow(label = "User Name", value = user?.userName ?: "Guest")
        ProfileInfoRow(label = "Email", value = user?.email ?: "N/A")
        ProfileInfoRow(label = "Roles", value = user?.roles?.joinToString { it.name } ?: "None")

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSync) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Me (Force Update)")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun MenuDivider(title: String, isExpanded: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        if (isExpanded) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = contentColor)
        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 16.dp),
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}