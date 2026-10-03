package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.RolesProvider
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.jwt.TokenValidationResult


class RefreshTokenUseCase(
    private val authRepository: AuthRepository,
    private val jwtProvider: JwtProviderContract,
    private val rolesProvider: RolesProvider
) {
    suspend operator fun invoke(): String? {
        val refreshToken = authRepository.getRefreshToken() ?: return null

        val decoded = when (val validation = jwtProvider.validateToken(refreshToken)) {
            is TokenValidationResult.Valid -> validation.decodedJWT
            is TokenValidationResult.Expired -> return null
            is TokenValidationResult.InvalidSignature -> return null
            is TokenValidationResult.MalformedToken -> return null
        }

        val userId = decoded.subject ?: return null

        val roles = rolesProvider.getRoles(userId)
        val newAccessToken = jwtProvider.generateAccessToken(userId, roles)

        authRepository.saveTokens(newAccessToken, refreshToken)

        return newAccessToken
    }
}
