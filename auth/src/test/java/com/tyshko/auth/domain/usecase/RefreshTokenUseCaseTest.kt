package com.tyshko.auth.domain.usecase

import com.auth0.jwt.interfaces.DecodedJWT
import com.tyshko.auth.domain.RolesProvider
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.jwt.JwtProvider
import com.tyshko.auth.security.jwt.TokenValidationResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RefreshTokenUseCaseTest {

    private lateinit var refreshTokenUseCase: RefreshTokenUseCase
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockJwtProvider: JwtProvider = mockk()
    private val mockRolesProvider: RolesProvider = mockk()

    @Before
    fun setup() {
        refreshTokenUseCase = RefreshTokenUseCase(
            mockAuthRepository,
            mockJwtProvider,
            mockRolesProvider
        )
    }

    @Test
    fun `invoke returns null when refresh token is missing`() = runTest {
        every { mockAuthRepository.getRefreshToken() } returns null
        assertNull(refreshTokenUseCase())
    }

    @Test
    fun `invoke returns null when refresh token is expired`() = runTest {
        every { mockAuthRepository.getRefreshToken() } returns "expired_token"
        every { mockJwtProvider.validateToken("expired_token") } returns TokenValidationResult.Expired
        assertNull(refreshTokenUseCase())
    }
    @Test
    fun `invoke returns null when refresh token has invalid signature`() = runTest {
        every { mockAuthRepository.getRefreshToken() } returns "forged_token"
        every { mockJwtProvider.validateToken("forged_token") } returns TokenValidationResult.InvalidSignature
        assertNull(refreshTokenUseCase())
    }
    @Test
    fun `invoke returns null when refresh token is malformed`() = runTest {
        every { mockAuthRepository.getRefreshToken() } returns "bad_token"
        every { mockJwtProvider.validateToken("bad_token") } returns TokenValidationResult.MalformedToken
        assertNull(refreshTokenUseCase())
    }
    @Test
    fun `invoke returns null when subject claim is missing`() = runTest {
        val mockDecoded: DecodedJWT = mockk { every { subject } returns null }
        every { mockAuthRepository.getRefreshToken() } returns "valid_token"
        every { mockJwtProvider.validateToken("valid_token") } returns TokenValidationResult.Valid(mockDecoded)
        assertNull(refreshTokenUseCase())
    }
    @Test
    fun `invoke returns new access token and saves tokens when refresh is valid`() = runTest {
        val userId = "user-123"
        val roles = listOf("User")
        val newAccessToken = "new_access_token"
        val mockDecoded: DecodedJWT = mockk { every { subject } returns userId }
        every { mockAuthRepository.getRefreshToken() } returns "valid_refresh"
        every { mockJwtProvider.validateToken("valid_refresh") } returns TokenValidationResult.Valid(mockDecoded)
        coEvery { mockRolesProvider.getRoles(userId) } returns roles
        every { mockJwtProvider.generateAccessToken(userId, roles) } returns newAccessToken
        val result = refreshTokenUseCase()
        assertEquals(newAccessToken, result)
        verify(exactly = 1) { mockAuthRepository.saveTokens(newAccessToken, "valid_refresh") }
    }
}
