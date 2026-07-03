package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.jwt.JwtProvider


class RefreshTokenUseCase(
    private val authRepository: AuthRepository,
    private val jwtProvider: JwtProvider
) {
    operator fun invoke(): String? {
        val refreshToken = authRepository.getRefreshToken() ?: return null

        val decoded = jwtProvider.validateToken(refreshToken) ?: return null

        val userId = decoded.subject ?: return null

        val newAccessToken = jwtProvider.generateAccessToken(userId, listOf("USER"))

        authRepository.saveTokens(newAccessToken, refreshToken)

        return newAccessToken
    }
}
