package com.tyshko.auth.data.repository

import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.data.local.dao.AuthDao
import com.tyshko.auth.data.local.entity.AuthCredentialEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var authRepository: AuthRepositoryImpl
    private val mockAuthDao: AuthDao = mockk()
    private val mockTokenStorage: TokenStorage = mockk(relaxed = true)

    @Before
    fun setup() {
        authRepository = AuthRepositoryImpl(mockAuthDao, mockTokenStorage)
    }

    @Test
    fun `checkEmailExists returns true when email exists`() = runTest {
        val email = "test@test.com"
        coEvery { mockAuthDao.isEmailExists(email) } returns true

        val result = authRepository.checkEmailExists(email)

        assertTrue(result)
        coVerify(exactly = 1) { mockAuthDao.isEmailExists(email) }
    }

    @Test
    fun `saveCredentials inserts entity correctly`() = runTest {
        val id = "1"
        val email = "test@test.com"
        val passwordHash = "hash"
        val expectedEntity = AuthCredentialEntity(id, email, passwordHash)

        coEvery { mockAuthDao.insertCredentials(expectedEntity) } returns Unit

        authRepository.saveCredentials(id, email, passwordHash)

        coVerify(exactly = 1) { mockAuthDao.insertCredentials(expectedEntity) }
    }

    @Test
    fun `getPasswordHash returns hash when email exists`() = runTest {
        val email = "test@test.com"
        val expectedHash = "hash123"
        coEvery { mockAuthDao.getPasswordHashByEmail(email) } returns expectedHash

        val result = authRepository.getPasswordHash(email)

        assertEquals(expectedHash, result)
        coVerify(exactly = 1) { mockAuthDao.getPasswordHashByEmail(email) }
    }

    @Test
    fun `getUserIdByEmail returns id when email exists`() = runTest {
        val email = "test@test.com"
        val expectedId = "user-123"
        coEvery { mockAuthDao.getUserIdByEmail(email) } returns expectedId

        val result = authRepository.getUserIdByEmail(email)

        assertEquals(expectedId, result)
        coVerify(exactly = 1) { mockAuthDao.getUserIdByEmail(email) }
    }

    @Test
    fun `saveTokens saves both access and refresh tokens`() {
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        authRepository.saveTokens(accessToken, refreshToken)

        verify(exactly = 1) { mockTokenStorage.saveAccessToken(accessToken) }
        verify(exactly = 1) { mockTokenStorage.saveRefreshToken(refreshToken) }
    }

    @Test
    fun `getRefreshToken returns token from storage`() {
        val expectedToken = "refresh_token_123"
        every { mockTokenStorage.getRefreshToken() } returns expectedToken

        val result = authRepository.getRefreshToken()

        assertEquals(expectedToken, result)
        verify(exactly = 1) { mockTokenStorage.getRefreshToken() }
    }

    @Test
    fun `clearSession clears tokens in storage`() {
        authRepository.clearSession()

        verify(exactly = 1) { mockTokenStorage.clearTokens() }
    }
}
