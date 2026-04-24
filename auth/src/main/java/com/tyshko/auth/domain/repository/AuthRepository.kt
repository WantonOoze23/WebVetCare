package com.tyshko.auth.domain.repository

interface AuthRepository {
    suspend fun checkEmailExists(email: String): Boolean
    suspend fun saveCredentials(id: String, email: String, passwordHash: String)
    suspend fun getPasswordHash(email: String): String?
    suspend fun getUserIdByEmail(email: String): String?

    fun saveTokens(accessToken: String, refreshToken: String)
    fun clearSession()
}