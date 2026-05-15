package com.tyshko.webvetcare.di

import com.tyshko.webvetcare.feature.auth.login.LoginViewModel
import com.tyshko.webvetcare.feature.auth.register.RegisterViewModel
import com.tyshko.webvetcare.feature.dashboard.UserDashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { UserDashboardViewModel(get(), get(), get()) }
}
