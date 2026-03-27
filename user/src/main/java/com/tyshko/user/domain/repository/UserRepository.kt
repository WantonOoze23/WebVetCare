package com.tyshko.user.domain.repository

import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.User

interface UserRepository {
    suspend fun getUser(userId: Long): User?

    suspend fun saveUser(user: User)
    suspend fun saveDoctorProfile(userId: Long, profile: DoctorProfile)
    suspend fun savePatientProfile(userId: Long, profile: PatientProfile)
}