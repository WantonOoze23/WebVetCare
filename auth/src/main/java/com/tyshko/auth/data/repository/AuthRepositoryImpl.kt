package com.tyshko.auth.data.repository

import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.data.local.dao.AuthDao
import com.tyshko.auth.data.local.entity.AuthCredentialEntity
import com.tyshko.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authDao: AuthDao,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun checkEmailExists(email: String): Boolean {
        return authDao.isEmailExists(email)
    }

    override suspend fun saveCredentials(id: String, email: String, passwordHash: String) {
        val entity = AuthCredentialEntity(
            id = id,
            email = email,
            passwordHash = passwordHash
        )
        authDao.insertCredentials(entity)
    }

    override suspend fun getPasswordHash(email: String): String? {
        return authDao.getPasswordHashByEmail(email)
    }

    override suspend fun getUserIdByEmail(email: String): String? {
        return authDao.getUserIdByEmail(email)
    }

    override fun saveTokens(accessToken: String, refreshToken: String) {
        tokenStorage.saveAccessToken(accessToken)
    }

    override fun clearSession() {
        tokenStorage.clearTokens()
    }
}