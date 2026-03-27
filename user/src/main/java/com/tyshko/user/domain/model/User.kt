package com.tyshko.user.domain.model

data class User(
    val id: Long,
    val userName: String,
    val email: String,
    val roles: List<Role>,
    val doctorProfile: DoctorProfile?,
    val patientProfile: PatientProfile?
)
