package com.tyshko.user.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctor_profiles")
data class DoctorProfileEntity(
    @PrimaryKey
    val userId: Long,
    val specialization: String,
    val clinicAddress: String,
    val licenseNumber: String,
    val availability: String
)