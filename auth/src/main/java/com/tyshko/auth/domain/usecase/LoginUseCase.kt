package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.PasswordHasherContract

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val jwtProvider: JwtProviderContract,
    private val passwordHasher: PasswordHasherContract
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return try {
            val storedHash = authRepository.getPasswordHash(email)
                ?: return AuthResult.Error.UserNotFound

            val isPasswordValid = passwordHasher.verifyPassword(password, storedHash)
            if (!isPasswordValid) {
                return AuthResult.Error.WrongPassword
            }

            val userId = authRepository.getUserIdByEmail(email)
                ?: return AuthResult.Error.UserDataCorrupted

            val accessToken = jwtProvider.generateAccessToken(userId, listOf("USER"))
            val refreshToken = jwtProvider.generateRefreshToken(userId)

            authRepository.saveTokens(accessToken, refreshToken)

            AuthResult.Success(userId)

        } catch (e: Exception) {
            AuthResult.Error.Unknown(e)
        }
    }
}