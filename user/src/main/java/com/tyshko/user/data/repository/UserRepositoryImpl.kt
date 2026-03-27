package com.tyshko.user.data.repository

import com.tyshko.user.data.local.dao.UserDao
import com.tyshko.user.data.local.entity.PatientProfileEntity
import com.tyshko.user.data.local.entity.UserEntity
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.User
import com.tyshko.user.domain.repository.UserRepository

class UserRepositoryImpl(
    private val UserDao: UserDao
) : UserRepository {
    override suspend fun getUser(userId: Long): User? {
        TODO("Not yet implemented")
    }

    override suspend fun saveUser(user: User) {
        val userEntity = UserEntity(
            id = user.id,
            userName = user.userName,
            email = user.email,
            roles = user.roles.map { it.name }
        )
    }

    override suspend fun saveDoctorProfile(
        userId: Long,
        profile: DoctorProfile
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun savePatientProfile(
        userId: Long,
        profile: PatientProfile
    ) {
        val patientEntity = PatientProfileEntity(
            userId = userId,
            contactPhoneNumber = profile.contactPhoneNumber,
            contactEmail = profile.contactEmail
        )
        UserDao.insertPatientProfile(patientEntity)
    }

}