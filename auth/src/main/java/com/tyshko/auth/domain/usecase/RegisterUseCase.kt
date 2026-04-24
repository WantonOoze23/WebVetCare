package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.crypto.PasswordHasher
import com.tyshko.auth.security.jwt.JwtProvider
import java.util.UUID

class RegisterUseCase(
    private val authRepository: AuthRepository,
    private val jwtProvider: JwtProvider
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return try {
            if (authRepository.checkEmailExists(email)) {
                return AuthResult.Error("User already exists")
            }

            val newUserId = UUID.randomUUID().toString()
            val hashedPassword = PasswordHasher.hashPassword(password)

            authRepository.saveCredentials(newUserId, email, hashedPassword)

            val accessToken = jwtProvider.generateAccessToken(newUserId, listOf("USER"))
            val refreshToken = jwtProvider.generateRefreshToken(newUserId)

            authRepository.saveTokens(accessToken, refreshToken)

            AuthResult.Success(newUserId)

        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }
}