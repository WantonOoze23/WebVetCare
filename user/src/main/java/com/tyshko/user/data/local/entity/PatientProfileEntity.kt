package com.tyshko.user.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profiles")
data class PatientProfileEntity(
    @PrimaryKey
    val userId: String,
    val contactEmail: String,
    val contactPhoneNumber: String
)