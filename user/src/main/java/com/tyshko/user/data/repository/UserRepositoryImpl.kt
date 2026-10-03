package com.tyshko.user.data.repository

import com.tyshko.user.data.local.dao.UserDao
import com.tyshko.user.data.local.entity.DoctorProfileEntity
import com.tyshko.user.data.local.entity.PatientProfileEntity
import com.tyshko.user.data.local.entity.UserEntity
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.model.User
import com.tyshko.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val UserDao: UserDao
) : UserRepository {
    override fun getUser(userId: String): Flow<User?> {
        return UserDao.getUserWithProfiles(userId).map { userWithProfile ->
            if (userWithProfile == null) return@map null

            User(
                id = userWithProfile.user.id,
                userName = userWithProfile.user.userName,
                email = userWithProfile.user.email,
                roles = userWithProfile.user.roles.mapNotNull { roleStr ->
                    runCatching { Role.valueOf(roleStr) }.getOrNull()
                },
                doctorProfile = userWithProfile.doctorProfile?.let {
                    DoctorProfile(
                        specialization = it.specialization,
                        licenseNumber = it.licenseNumber,
                        clinicAddress = it.clinicAddress,
                        availability = it.availability
                    )
                },
                patientProfile = userWithProfile.patientProfile?.let {
                    PatientProfile(
                        contactPhoneNumber = it.contactPhoneNumber,
                        contactEmail = it.contactEmail
                    )
                }
            )
        }
    }

    override suspend fun saveUser(user: User) {
        val userToSave = UserEntity(
            id = user.id,
            userName = user.userName,
            email = user.email,
            roles = user.roles.map { it.name }
        )

        UserDao.insertUser(userToSave)

        user.patientProfile?.let{
            savePatientProfile(
                userId = user.id,
                profile = it
            )
        }
        user.doctorProfile?.let{
            saveDoctorProfile(
                userId = user.id,
                profile = it
            )
        }
    }

    override suspend fun saveDoctorProfile(
        userId: String,
        profile: DoctorProfile
    ) {
        val doctorToSave = DoctorProfileEntity(
            userId = userId,
            specialization = profile.specialization,
            clinicAddress = profile.clinicAddress,
            licenseNumber = profile.licenseNumber,
            availability = profile.availability
        )

        UserDao.insertDoctorProfile(doctorToSave)
    }

    override suspend fun savePatientProfile(
        userId: String,
        profile: PatientProfile
    ) {
        val patientToSave = PatientProfileEntity(
            userId = userId,
            contactPhoneNumber = profile.contactPhoneNumber,
            contactEmail = profile.contactEmail
        )

        UserDao.insertPatientProfile(patientToSave)
    }

}