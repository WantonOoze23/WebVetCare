package com.tyshko.config.domain.repository

import com.tyshko.config.domain.model.SettingsConfig

interface SettingsRepository {
    fun getConfig(): SettingsConfig
    suspend fun fetchAndActivate(): Boolean
    fun addConfigUpdateListener(onUpdate: () -> Unit): AutoCloseable

}