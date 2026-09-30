package com.tyshko.auth.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auth_credentials",
    indices = [Index(value = ["email"], unique = true)]
)
data class AuthCredentialEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String
)