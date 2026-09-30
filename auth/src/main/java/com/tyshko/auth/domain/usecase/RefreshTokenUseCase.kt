package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.RolesProvider
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.jwt.JwtProvider


class RefreshTokenUseCase(
    private val authRepository: AuthRepository,
    private val jwtProvider: JwtProvider,
    private val rolesProvider: RolesProvider
) {
    suspend operator fun invoke(): String? {
        val refreshToken = authRepository.getRefreshToken() ?: return null

        val decoded = jwtProvider.validateToken(refreshToken) ?: return null

        val userId = decoded.subject ?: return null

        val roles = rolesProvider.getRoles(userId)
        val newAccessToken = jwtProvider.generateAccessToken(userId, roles)

        authRepository.saveTokens(newAccessToken, refreshToken)

        return newAccessToken
    }
}
