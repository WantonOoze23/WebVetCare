package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.crypto.PasswordHasher
import com.tyshko.auth.security.jwt.JwtProvider

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val jwtProvider: JwtProvider
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return try {
            val storedHash = authRepository.getPasswordHash(email)
                ?: return AuthResult.Error("Incorrect input")

            val isPasswordValid = PasswordHasher.verifyPassword(password, storedHash)
            if (!isPasswordValid) {
                return AuthResult.Error("Wrong password")
            }

            val userId = authRepository.getUserIdByEmail(email)
                ?: return AuthResult.Error("User data error")

            val accessToken = jwtProvider.generateAccessToken(userId, listOf("USER"))
            val refreshToken = jwtProvider.generateRefreshToken(userId)

            authRepository.saveTokens(accessToken, refreshToken)

            AuthResult.Success(userId)

        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.message}")
        }
    }
}