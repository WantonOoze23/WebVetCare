package com.tyshko.user.data.repository

import com.tyshko.user.data.local.dao.UserDao
import com.tyshko.user.data.local.entity.DoctorProfileEntity
import com.tyshko.user.data.local.entity.PatientProfileEntity
import com.tyshko.user.data.local.entity.UserEntity
import com.tyshko.user.data.local.entity.UserWithProfile
import com.tyshko.user.domain.model.DoctorProfile
import com.tyshko.user.domain.model.PatientProfile
import com.tyshko.user.domain.model.Role
import com.tyshko.user.domain.model.User
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class UserRepositoryImplTest {

    private lateinit var userRepository: UserRepositoryImpl
    private val mockUserDao: UserDao = mockk(relaxed = true)

    @Before
    fun setup() {
        userRepository = UserRepositoryImpl(mockUserDao)
    }

    @Test
    fun `getUser should return null when user not found`() = runTest {
        val userId = "123"
        every { mockUserDao.getUserWithProfiles(userId) } returns flowOf(null)

        val result = userRepository.getUser(userId).first()

        assertNull(result)
    }

    @Test
    fun `getUser should return mapped User when found`() = runTest {
        val userId = "123"
        val userEntity = UserEntity(userId, "TestUser", "test@test.com", listOf("User", "Doctor"))
        val doctorProfileEntity = DoctorProfileEntity(userId, "Cats", "Address", "123", "Yes")
        val patientProfileEntity = PatientProfileEntity(userId, "p@test.com", "000")
        val userWithProfiles = UserWithProfile(userEntity, doctorProfileEntity, patientProfileEntity)

        every { mockUserDao.getUserWithProfiles(userId) } returns flowOf(userWithProfiles)

        val result = userRepository.getUser(userId).first()

        assertNotNull(result)
        assertEquals(userId, result?.id)
        assertEquals("TestUser", result?.userName)
        assertEquals("test@test.com", result?.email)
        assertEquals(listOf(Role.User, Role.Doctor), result?.roles)
        assertEquals("Cats", result?.doctorProfile?.specialization)
        assertEquals("000", result?.patientProfile?.contactPhoneNumber)
    }

    @Test
    fun `saveUser should save user entity and profiles if present`() = runTest {
        val userId = "123"
        val doctorProfile = DoctorProfile("Dogs", "456", "Address", "No")
        val patientProfile = PatientProfile("111", "test@test.com")
        val user = User(userId, "User", "test@test.com", listOf(Role.User), doctorProfile, patientProfile)

        userRepository.saveUser(user)

        val expectedUserEntity = UserEntity(userId, "User", "test@test.com", listOf("User"))
        coVerify(exactly = 1) { mockUserDao.insertUser(expectedUserEntity) }
        
        val expectedDoctorEntity = DoctorProfileEntity(userId, "Dogs", "Address", "456", "No")
        coVerify(exactly = 1) { mockUserDao.insertDoctorProfile(expectedDoctorEntity) }
        
        val expectedPatientEntity = PatientProfileEntity(userId, "test@test.com", "111")
        coVerify(exactly = 1) { mockUserDao.insertPatientProfile(expectedPatientEntity) }
    }

    @Test
    fun `saveDoctorProfile should insert DoctorProfileEntity correctly`() = runTest {
        val userId = "123"
        val profile = DoctorProfile("Birds", "789", "Vet", "Always")

        userRepository.saveDoctorProfile(userId, profile)

        val expectedEntity = DoctorProfileEntity(userId, "Birds", "Vet", "789", "Always")
        coVerify(exactly = 1) { mockUserDao.insertDoctorProfile(expectedEntity) }
    }

    @Test
    fun `savePatientProfile should insert PatientProfileEntity correctly`() = runTest {
        val userId = "123"
        val profile = PatientProfile("222", "test2@test.com")

        userRepository.savePatientProfile(userId, profile)

        val expectedEntity = PatientProfileEntity(userId, "test2@test.com", "222")
        coVerify(exactly = 1) { mockUserDao.insertPatientProfile(expectedEntity) }
    }

    @Test
    fun `getUser should return mapped User without profiles when profiles are null`() = runTest {
        val userId = "123"
        val userEntity = UserEntity(userId, "TestUser", "test@test.com", listOf("User"))
        val userWithProfiles = UserWithProfile(userEntity, null, null)

        every { mockUserDao.getUserWithProfiles(userId) } returns flowOf(userWithProfiles)

        val result = userRepository.getUser(userId).first()

        assertNotNull(result)
        assertEquals(userId, result?.id)
        assertNull(result?.doctorProfile)
        assertNull(result?.patientProfile)
    }

    @Test
    fun `getUser should ignore unknown roles and map valid ones`() = runTest {
        val userId = "123"
        val userEntity = UserEntity(userId, "TestUser", "test@test.com", listOf("User", "UNKNOWN_ROLE", "Doctor"))
        val userWithProfiles = UserWithProfile(userEntity, null, null)

        every { mockUserDao.getUserWithProfiles(userId) } returns flowOf(userWithProfiles)

        val result = userRepository.getUser(userId).first()

        assertEquals(listOf(Role.User, Role.Doctor), result?.roles)
    }

    @Test
    fun `saveUser should only insert user entity when profiles are null`() = runTest {
        val userId = "123"
        val user = User(userId, "User", "test@test.com", listOf(Role.User), null, null)

        userRepository.saveUser(user)

        val expectedUserEntity = UserEntity(userId, "User", "test@test.com", listOf("User"))
        coVerify(exactly = 1) { mockUserDao.insertUser(expectedUserEntity) }
        
        coVerify(exactly = 0) { mockUserDao.insertDoctorProfile(any()) }
        coVerify(exactly = 0) { mockUserDao.insertPatientProfile(any()) }
    }
}
