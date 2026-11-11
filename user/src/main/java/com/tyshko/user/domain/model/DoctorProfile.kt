package com.tyshko.user.domain.model

data class DoctorProfile(
    val specialization: String,
    val licenseNumber: String,
    val clinicAddress: String,
    val availability: String
)
