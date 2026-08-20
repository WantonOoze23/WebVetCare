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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.model.User
import com.tyshko.webvetcare.feature.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserDashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = {},
    viewModel: UserDashboardViewModel = koinViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is DashboardContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is DashboardContract.Effect.NavigateToLogin -> onNavigateToLogin()
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
                currentRoute = state.currentRoute,
                onRouteSelected = { viewModel.onEvent(DashboardContract.Event.ChangeRoute(it)) },
                onToggleMenu = { viewModel.onEvent(DashboardContract.Event.ToggleMenu) },
                onLogout = { viewModel.onEvent(DashboardContract.Event.OnLogout) }
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

                // Динамическое переключение контента на основе выбранного роута
                when (state.currentRoute) {
                    DashboardContract.DashboardRoute.UserScreen -> {
                        UserProfileContent(
                            user = state.user,
                            onSync = { viewModel.onEvent(DashboardContract.Event.FetchUser) }
                        )
                    }
                    DashboardContract.DashboardRoute.BecomeDoctor -> {
                        BecomeDoctorForm(
                            onSubmit = { viewModel.onEvent(DashboardContract.Event.BecomeDoctor(it)) }
                        )
                    }
                    DashboardContract.DashboardRoute.DoctorProfile -> {
                        DoctorProfileContent(profile = state.user?.doctorProfile)
                    }
                    DashboardContract.DashboardRoute.BecomePatient -> {
                        BecomePatientForm(
                            onSubmit = { viewModel.onEvent(DashboardContract.Event.BecomePatient(it)) }
                        )
                    }
                    DashboardContract.DashboardRoute.PatientProfile -> {
                        PatientProfileContent(profile = state.user?.patientProfile)
                    }
                    DashboardContract.DashboardRoute.Settings -> {
                        SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun SideNavigationMenu(
    isExpanded: Boolean,
    userRoles: List<Role>,
    currentRoute: DashboardContract.DashboardRoute,
    onRouteSelected: (DashboardContract.DashboardRoute) -> Unit,
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
            isSelected = currentRoute == DashboardContract.DashboardRoute.UserScreen,
            isExpanded = isExpanded,
            onClick = { onRouteSelected(DashboardContract.DashboardRoute.UserScreen) }
        )

        // Группа: Patient
        MenuDivider(title = "PATIENT", isExpanded = isExpanded)
        if (userRoles.contains(Role.Patient)) {
            MenuItem(
                icon = Icons.Default.Favorite,
                text = "Patient Data",
                isSelected = currentRoute == DashboardContract.DashboardRoute.PatientProfile,
                isExpanded = isExpanded,
                onClick = { onRouteSelected(DashboardContract.DashboardRoute.PatientProfile) }
            )
        } else {
            MenuItem(
                icon = Icons.Default.Add,
                text = "Become Patient",
                isSelected = currentRoute == DashboardContract.DashboardRoute.BecomePatient,
                isExpanded = isExpanded,
                onClick = { onRouteSelected(DashboardContract.DashboardRoute.BecomePatient) }
            )
        }

        // Группа: Doctor
        MenuDivider(title = "DOCTOR", isExpanded = isExpanded)
        if (userRoles.contains(Role.Doctor)) {
            MenuItem(
                icon = Icons.Default.AccountBox,
                text = "Doctor Data",
                isSelected = currentRoute == DashboardContract.DashboardRoute.DoctorProfile,
                isExpanded = isExpanded,
                onClick = { onRouteSelected(DashboardContract.DashboardRoute.DoctorProfile) }
            )
        } else {
            MenuItem(
                icon = Icons.Default.Build,
                text = "Become Doctor",
                isSelected = currentRoute == DashboardContract.DashboardRoute.BecomeDoctor,
                isExpanded = isExpanded,
                onClick = { onRouteSelected(DashboardContract.DashboardRoute.BecomeDoctor) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Settings (visible for all roles)
        MenuItem(
            icon = Icons.Default.Settings,
            text = "Settings",
            isSelected = currentRoute == DashboardContract.DashboardRoute.Settings,
            isExpanded = isExpanded,
            onClick = { onRouteSelected(DashboardContract.DashboardRoute.Settings) }
        )

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
fun BecomeDoctorForm(onSubmit: (DoctorProfile) -> Unit) {
    var specialization by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Become a Doctor", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("License Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = clinicAddress, onValueChange = { clinicAddress = it }, label = { Text("Clinic Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("Availability") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = { onSubmit(DoctorProfile(specialization, licenseNumber, clinicAddress, availability)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = specialization.isNotBlank() && licenseNumber.isNotBlank()
        ) {
            Text("Submit Doctor Application")
        }
    }
}

@Composable
fun DoctorProfileContent(profile: DoctorProfile?) {
    if (profile == null) {
        Text("No doctor profile found.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Doctor Information", style = MaterialTheme.typography.titleLarge)
            ProfileInfoRow(label = "Specialization", value = profile.specialization)
            ProfileInfoRow(label = "License", value = profile.licenseNumber)
            ProfileInfoRow(label = "Clinic", value = profile.clinicAddress)
            ProfileInfoRow(label = "Availability", value = profile.availability)
        }
    }
}

@Composable
fun BecomePatientForm(onSubmit: (PatientProfile) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Become a Patient", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = { onSubmit(PatientProfile(phone, email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = phone.isNotBlank() && email.isNotBlank()
        ) {
            Text("Submit Patient Application")
        }
    }
}

@Composable
fun PatientProfileContent(profile: PatientProfile?) {
    if (profile == null) {
        Text("No patient profile found.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Patient Information", style = MaterialTheme.typography.titleLarge)
            ProfileInfoRow(label = "Phone", value = profile.contactPhoneNumber)
            ProfileInfoRow(label = "Email", value = profile.contactEmail)
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
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
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