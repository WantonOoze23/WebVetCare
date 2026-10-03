package com.tyshko.auth.data.repository

import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.data.local.dao.AuthDao
import com.tyshko.auth.data.local.entity.AuthCredentialEntity
import com.tyshko.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authDao: AuthDao,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private fun String.normalizeEmail() = this.trim().lowercase()


    override suspend fun checkEmailExists(email: String): Boolean {
        return authDao.isEmailExists(email.normalizeEmail())
    }

    override suspend fun saveCredentials(id: String, email: String, passwordHash: String) {
        val entity = AuthCredentialEntity(
            id = id,
            email = email.normalizeEmail(),
            passwordHash = passwordHash
        )
        authDao.insertCredentials(entity)
    }

    override suspend fun getPasswordHash(email: String): String? {
        return authDao.getPasswordHashByEmail(email.normalizeEmail())
    }

    override suspend fun getUserIdByEmail(email: String): String? {
        return authDao.getUserIdByEmail(email.normalizeEmail())
    }

    override fun saveTokens(accessToken: String, refreshToken: String) {
        tokenStorage.saveAccessToken(accessToken)
        tokenStorage.saveRefreshToken(refreshToken)
    }

    override fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken()
    }

    override fun clearSession() {
        tokenStorage.clearTokens()
    }
}