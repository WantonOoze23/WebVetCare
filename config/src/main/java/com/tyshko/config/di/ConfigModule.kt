package com.tyshko.config.di

import com.tyshko.config.BuildConfig
import com.tyshko.config.data.repository.FirebaseSettingsRepository
import com.tyshko.config.domain.repository.SettingsRepository
import org.koin.dsl.module

val configModule = module {
    single { FirebaseSettingsRepository(debug = BuildConfig.DEBUG) as SettingsRepository }
}