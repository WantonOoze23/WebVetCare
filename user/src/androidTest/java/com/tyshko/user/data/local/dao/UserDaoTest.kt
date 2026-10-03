package com.tyshko.user.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tyshko.user.data.local.db.UserDatabase
import com.tyshko.user.data.local.entity.DoctorProfileEntity
import com.tyshko.user.data.local.entity.PatientProfileEntity
import com.tyshko.user.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getUserWithProfiles_returns_user_and_associated_profiles() = runTest {

        val userId = "1"
        val userEntity = UserEntity(userId, "TestUser", "test@test.com", listOf("User", "Doctor", "Patient"))
        val doctorEntity = DoctorProfileEntity(userId, "Vet", "Address", "123", "Yes")
        val patientEntity = PatientProfileEntity(userId, "p@test.com", "12345")

        userDao.insertUser(userEntity)
        userDao.insertDoctorProfile(doctorEntity)
        userDao.insertPatientProfile(patientEntity)
        
        val result = userDao.getUserWithProfiles(userId).first()

        assertNotNull(result)
        assertEquals(userId, result?.user?.id)
        assertEquals("Vet", result?.doctorProfile?.specialization)
        assertEquals("12345", result?.patientProfile?.contactPhoneNumber)
    }

    @Test
    fun getUserWithProfiles_returns_null_profiles_when_not_present() = runTest {

        val userId = "2"
        val userEntity = UserEntity(userId, "TestUser", "test@test.com", listOf("User"))
        userDao.insertUser(userEntity)

        val result = userDao.getUserWithProfiles(userId).first()

        assertNotNull(result)
        assertEquals(userId, result?.user?.id)
        assertNull(result?.doctorProfile)
        assertNull(result?.patientProfile)
    }

    @Test
    fun getUserWithProfiles_returns_null_when_user_does_not_exist() = runTest {

        val result = userDao.getUserWithProfiles("non_existent_id").first()

        assertNull(result)
    }

    @Test
    fun insertUser_replaces_existing_user_on_conflict() = runTest {

        val userId = "3"
        val userEntity = UserEntity(userId, "OriginalName", "test@test.com", listOf("User"))
        userDao.insertUser(userEntity)

        val updatedUserEntity = UserEntity(userId, "UpdatedName", "new@test.com", listOf("User", "Doctor"))
        userDao.insertUser(updatedUserEntity)

        val result = userDao.getUserWithProfiles(userId).first()
        assertEquals("UpdatedName", result?.user?.userName)
        assertEquals("new@test.com", result?.user?.email)
        assertEquals(listOf("User", "Doctor"), result?.user?.roles)
    }

    @Test
    fun clearAllUsers_removes_all_users_from_db() = runTest {

        val user1 = UserEntity("10", "User1", "test1@test.com", listOf("User"))
        val user2 = UserEntity("20", "User2", "test2@test.com", listOf("User"))
        userDao.insertUser(user1)
        userDao.insertUser(user2)

        userDao.clearAllUsers()

        val result1 = userDao.getUserWithProfiles("10").first()
        val result2 = userDao.getUserWithProfiles("20").first()
        
        assertNull(result1)
        assertNull(result2)
    }
}
