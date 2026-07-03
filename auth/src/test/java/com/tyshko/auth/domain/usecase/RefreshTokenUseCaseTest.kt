package com.tyshko.auth.domain.usecase

import com.auth0.jwt.interfaces.DecodedJWT
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.jwt.JwtProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RefreshTokenUseCaseTest {

    private lateinit var refreshTokenUseCase: RefreshTokenUseCase
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockJwtProvider: JwtProvider = mockk()

    @Before
    fun setup() {
        refreshTokenUseCase = RefreshTokenUseCase(mockAuthRepository, mockJwtProvider)
    }

    @Test
    fun `invoke should return null when refresh token is missing`() {
        every { mockAuthRepository.getRefreshToken() } returns null

        val result = refreshTokenUseCase()

        assertNull(result)
    }

    @Test
    fun `invoke should return null when refresh token is invalid`() {
        val invalidToken = "invalid_token"
        every { mockAuthRepository.getRefreshToken() } returns invalidToken
        every { mockJwtProvider.validateToken(invalidToken) } returns null

        val result = refreshTokenUseCase()

        assertNull(result)
    }

    @Test
    fun `invoke should return null when subject is missing in decoded token`() {
        val validToken = "valid_token"
        val mockDecodedJWT: DecodedJWT = mockk()

        every { mockAuthRepository.getRefreshToken() } returns validToken
        every { mockJwtProvider.validateToken(validToken) } returns mockDecodedJWT
        every { mockDecodedJWT.subject } returns null

        val result = refreshTokenUseCase()

        assertNull(result)
    }

    @Test
    fun `invoke should return new access token and save tokens when refresh token is valid`() {
        val validRefreshToken = "valid_refresh_token"
        val mockDecodedJWT: DecodedJWT = mockk()
        val userId = "user-123"
        val newAccessToken = "new_access_token"

        every { mockAuthRepository.getRefreshToken() } returns validRefreshToken
        every { mockJwtProvider.validateToken(validRefreshToken) } returns mockDecodedJWT
        every { mockDecodedJWT.subject } returns userId
        every { mockJwtProvider.generateAccessToken(userId, listOf("USER")) } returns newAccessToken

        val result = refreshTokenUseCase()

        assertEquals(newAccessToken, result)
        
        verify(exactly = 1) { mockAuthRepository.saveTokens(newAccessToken, validRefreshToken) }
    }
}
