package com.tyshko.auth.domain.usecase

import com.tyshko.auth.domain.model.AuthResult
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.security.crypto.PasswordHasher
import com.tyshko.auth.security.jwt.JwtProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var loginUseCase: LoginUseCase
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockJwtProvider: JwtProvider = mockk()

    @Before
    fun setup() {
        loginUseCase = LoginUseCase(mockAuthRepository, mockJwtProvider)
        mockkObject(PasswordHasher)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke should return Error when email does not exist`() = runTest {
        val email = "unknown@test.com"
        val password = "password"

        coEvery { mockAuthRepository.getPasswordHash(email) } returns null

        val result = loginUseCase(email, password)

        assertTrue(result is AuthResult.Error)
        assertEquals("Incorrect input", (result as AuthResult.Error).message)
    }

    @Test
    fun `invoke should return Error when password is incorrect`() = runTest {
        val email = "user@test.com"
        val password = "wrong_password"
        val storedHash = "stored_hash"

        coEvery { mockAuthRepository.getPasswordHash(email) } returns storedHash
        every { PasswordHasher.verifyPassword(password, storedHash) } returns false

        val result = loginUseCase(email, password)

        assertTrue(result is AuthResult.Error)
        assertEquals("Wrong password", (result as AuthResult.Error).message)
    }

    @Test
    fun `invoke should return Error when user id not found after valid password`() = runTest {
        val email = "user@test.com"
        val password = "password"
        val storedHash = "stored_hash"

        coEvery { mockAuthRepository.getPasswordHash(email) } returns storedHash
        every { PasswordHasher.verifyPassword(password, storedHash) } returns true
        coEvery { mockAuthRepository.getUserIdByEmail(email) } returns null

        val result = loginUseCase(email, password)

        assertTrue(result is AuthResult.Error)
        assertEquals("User data error", (result as AuthResult.Error).message)
    }

    @Test
    fun `invoke should return Success and save tokens when credentials are valid`() = runTest {
        val email = "user@test.com"
        val password = "password"
        val storedHash = "stored_hash"
        val userId = "user-123"
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        coEvery { mockAuthRepository.getPasswordHash(email) } returns storedHash
        every { PasswordHasher.verifyPassword(password, storedHash) } returns true
        coEvery { mockAuthRepository.getUserIdByEmail(email) } returns userId
        every { mockJwtProvider.generateAccessToken(userId, any()) } returns accessToken
        every { mockJwtProvider.generateRefreshToken(userId) } returns refreshToken

        val result = loginUseCase(email, password)

        assertTrue(result is AuthResult.Success)
        assertEquals(userId, (result as AuthResult.Success).userId)
        
        coVerify(exactly = 1) { mockAuthRepository.saveTokens(accessToken, refreshToken) }
    }
}
