package com.tyshko.webvetcare.firebase.domain.model

data class SettingsConfig(
    val testData : String,
    val landingColor : String,
    val welcomeMessage : String,
    val isOnline : Boolean
)