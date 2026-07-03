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

class RegisterUseCaseTest {

    private lateinit var registerUseCase: RegisterUseCase
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockJwtProvider: JwtProvider = mockk()

    @Before
    fun setup() {
        registerUseCase = RegisterUseCase(mockAuthRepository, mockJwtProvider)
        mockkObject(PasswordHasher)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke should return Error when email already exists`() = runTest {
        val email = "existing@test.com"
        val password = "password"

        coEvery { mockAuthRepository.checkEmailExists(email) } returns true

        val result = registerUseCase(email, password)

        assertTrue(result is AuthResult.Error)
        assertEquals("User already exists", (result as AuthResult.Error).message)
    }

    @Test
    fun `invoke should return Success and save credentials and tokens for new user`() = runTest {
        val email = "new@test.com"
        val password = "password"
        val hashedPassword = "hashed_password"
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        coEvery { mockAuthRepository.checkEmailExists(email) } returns false
        every { PasswordHasher.hashPassword(password) } returns hashedPassword
        
        every { mockJwtProvider.generateAccessToken(any(), any()) } returns accessToken
        every { mockJwtProvider.generateRefreshToken(any()) } returns refreshToken

        val result = registerUseCase(email, password)

        assertTrue(result is AuthResult.Success)
        
        coVerify(exactly = 1) { mockAuthRepository.saveCredentials(any(), email, hashedPassword) }
        coVerify(exactly = 1) { mockAuthRepository.saveTokens(accessToken, refreshToken) }
    }
}
