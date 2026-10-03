package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.PasswordHasherContract
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var loginUseCase: LoginUseCase
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockJwtProvider: JwtProviderContract = mockk()
    private val mockPasswordHasher: PasswordHasherContract = mockk()

    @Before
    fun setup() {
        loginUseCase = LoginUseCase(
            authRepository = mockAuthRepository,
            jwtProvider = mockJwtProvider,
            passwordHasher = mockPasswordHasher
        )
    }

    @Test
    fun `invoke returns UserNotFound when email has no password hash`() = runTest {
        coEvery { mockAuthRepository.getPasswordHash(any()) } returns null

        val result = loginUseCase("unknown@test.com", "password")

        assertTrue(result is AuthResult.Error.UserNotFound)
    }

    @Test
    fun `invoke returns WrongPassword when password does not match`() = runTest {
        val storedHash = "stored_hash"
        coEvery { mockAuthRepository.getPasswordHash(any()) } returns storedHash
        every { mockPasswordHasher.verifyPassword("wrong", storedHash) } returns false

        val result = loginUseCase("user@test.com", "wrong")

        assertTrue(result is AuthResult.Error.WrongPassword)
    }

    @Test
    fun `invoke returns UserDataCorrupted when userId is missing after valid password`() = runTest {
        val storedHash = "stored_hash"
        coEvery { mockAuthRepository.getPasswordHash(any()) } returns storedHash
        every { mockPasswordHasher.verifyPassword("password", storedHash) } returns true
        coEvery { mockAuthRepository.getUserIdByEmail(any()) } returns null

        val result = loginUseCase("user@test.com", "password")

        assertTrue(result is AuthResult.Error.UserDataCorrupted)
    }

    @Test
    fun `invoke returns Success and saves tokens when credentials are valid`() = runTest {
        val email = "user@test.com"
        val password = "password"
        val storedHash = "stored_hash"
        val userId = "user-123"
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        coEvery { mockAuthRepository.getPasswordHash(email) } returns storedHash
        every { mockPasswordHasher.verifyPassword(password, storedHash) } returns true
        coEvery { mockAuthRepository.getUserIdByEmail(email) } returns userId
        every { mockJwtProvider.generateAccessToken(userId, any()) } returns accessToken
        every { mockJwtProvider.generateRefreshToken(userId) } returns refreshToken

        val result = loginUseCase(email, password)

        assertTrue(result is AuthResult.Success)
        assertEquals(userId, (result as AuthResult.Success).userId)
        coVerify(exactly = 1) { mockAuthRepository.saveTokens(accessToken, refreshToken) }
    }

    @Test
    fun `invoke returns Unknown error when repository throws exception`() = runTest {
        val exception = RuntimeException("DB unavailable")
        coEvery { mockAuthRepository.getPasswordHash(any()) } throws exception

        val result = loginUseCase("user@test.com", "password")

        assertTrue(result is AuthResult.Error.Unknown)
        assertEquals(exception, (result as AuthResult.Error.Unknown).cause)
    }
}
