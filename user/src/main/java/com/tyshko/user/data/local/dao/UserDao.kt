package com.tyshko.user.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.tyshko.user.data.local.entity.DoctorProfileEntity
import com.tyshko.user.data.local.entity.PatientProfileEntity
import com.tyshko.user.data.local.entity.UserEntity
import com.tyshko.user.data.local.entity.UserWithProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithProfiles(userId: String): Flow<UserWithProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorProfile(profile: DoctorProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientProfile(profile: PatientProfileEntity)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}