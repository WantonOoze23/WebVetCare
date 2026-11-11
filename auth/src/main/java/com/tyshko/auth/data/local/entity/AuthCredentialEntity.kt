package com.tyshko.auth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_credentials")
data class AuthCredentialEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String
)