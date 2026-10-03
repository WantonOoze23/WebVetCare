package com.tyshko.webvetcare.di

import com.tyshko.auth.domain.RolesProvider
import com.tyshko.webvetcare.feature.auth.UserRolesProvider
import com.tyshko.webvetcare.feature.auth.login.LoginViewModel
import com.tyshko.webvetcare.feature.auth.register.RegisterViewModel
import com.tyshko.webvetcare.feature.dashboard.UserDashboardViewModel
import com.tyshko.webvetcare.navigation.SessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { UserDashboardViewModel(get(), get(), get(), get()) }
    viewModel { SessionViewModel(get(), get(), get()) }
    single<RolesProvider> { UserRolesProvider(get()) }
}
