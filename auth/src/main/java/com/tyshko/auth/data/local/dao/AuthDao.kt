package com.tyshko.auth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tyshko.auth.data.local.entity.AuthCredentialEntity

@Dao
interface AuthDao {

    @Query("SELECT EXISTS(SELECT 1 FROM auth_credentials WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean

    @Query("SELECT passwordHash FROM auth_credentials WHERE email = :email")
    suspend fun getPasswordHashByEmail(email: String): String?

    @Query("SELECT id FROM auth_credentials WHERE email = :email")
    suspend fun getUserIdByEmail(email: String): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCredentials(entity: AuthCredentialEntity)
}