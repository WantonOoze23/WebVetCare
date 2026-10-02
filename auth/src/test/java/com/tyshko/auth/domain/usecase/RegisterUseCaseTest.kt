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

class RegisterUseCaseTest {

    private lateinit var registerUseCase: RegisterUseCase
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockJwtProvider: JwtProviderContract = mockk()
    private val mockPasswordHasher: PasswordHasherContract = mockk()

    @Before
    fun setup() {
        registerUseCase = RegisterUseCase(
            authRepository = mockAuthRepository,
            jwtProvider = mockJwtProvider,
            passwordHasher = mockPasswordHasher
        )
    }

    @Test
    fun `invoke returns UserAlreadyExists when email is taken`() = runTest {
        coEvery { mockAuthRepository.checkEmailExists(any()) } returns true

        val result = registerUseCase("existing@test.com", "password")

        assertTrue(result is AuthResult.Error.UserAlreadyExists)
    }

    @Test
    fun `invoke returns Success and saves credentials and tokens for new user`() = runTest {
        val email = "new@test.com"
        val password = "password"
        val hashedPassword = "hashed_password"
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        coEvery { mockAuthRepository.checkEmailExists(any()) } returns false
        // mockPasswordHasher — обычный interface mock, no mockkObject/Byte Buddy
        every { mockPasswordHasher.hashPassword(password) } returns hashedPassword
        every { mockJwtProvider.generateAccessToken(any(), any()) } returns accessToken
        every { mockJwtProvider.generateRefreshToken(any()) } returns refreshToken

        val result = registerUseCase(email, password)

        assertTrue(result is AuthResult.Success)
        coVerify(exactly = 1) { mockAuthRepository.saveCredentials(any(), email, hashedPassword) }
        coVerify(exactly = 1) { mockAuthRepository.saveTokens(accessToken, refreshToken) }
    }

    @Test
    fun `invoke returns Unknown error when repository throws exception`() = runTest {
        val exception = RuntimeException("DB error")
        coEvery { mockAuthRepository.checkEmailExists(any()) } throws exception

        val result = registerUseCase("user@test.com", "password")

        assertTrue(result is AuthResult.Error.Unknown)
        assertEquals(exception, (result as AuthResult.Error.Unknown).cause)
    }
}
