package com.tyshko.config.domain.model

data class SettingsConfig(
    val appLanguage: String,
    val testData : String,
    val landingColor : String,
    val welcomeMessage : String,
    val isOnline : Boolean
)