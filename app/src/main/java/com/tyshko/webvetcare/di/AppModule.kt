package com.tyshko.webvetcare.di

import com.tyshko.webvetcare.feature.auth.login.LoginViewModel
import com.tyshko.webvetcare.feature.auth.register.RegisterViewModel
import com.tyshko.webvetcare.feature.dashboard.UserDashboardViewModel
import com.tyshko.webvetcare.feature.settings.SettingsViewModel
import com.tyshko.webvetcare.firebase.data.repository.FirebaseSettingsRepository
import com.tyshko.webvetcare.firebase.domain.repository.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseSettingsRepository() as SettingsRepository }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { UserDashboardViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
