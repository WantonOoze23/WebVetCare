package com.tyshko.webvetcare.firebase.domain.repository

interface SettingsRepository {
    fun getTestData(): String
    fun getLandingColor(): String
    fun getWelcomeMessage(): String
    fun isOnline(): Boolean
    suspend fun fetchAndActivate(): Boolean
    fun getConfigUpdateListener(onUpdate: () -> Unit)
}