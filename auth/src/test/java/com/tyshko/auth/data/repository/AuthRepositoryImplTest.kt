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
        coEvery { mockAuthDao.isEmailExists("test@test.com") } returns true

        val result = authRepository.checkEmailExists("test@test.com")

        assertTrue(result)
        coVerify(exactly = 1) { mockAuthDao.isEmailExists("test@test.com") }
    }

    @Test
    fun `checkEmailExists normalizes email to lowercase`() = runTest {
        coEvery { mockAuthDao.isEmailExists("user@test.com") } returns false

        authRepository.checkEmailExists("User@TEST.com")

        coVerify(exactly = 1) { mockAuthDao.isEmailExists("user@test.com") }
    }

    @Test
    fun `checkEmailExists trims whitespace before querying`() = runTest {
        coEvery { mockAuthDao.isEmailExists("user@test.com") } returns true

        authRepository.checkEmailExists("  user@test.com  ")

        coVerify(exactly = 1) { mockAuthDao.isEmailExists("user@test.com") }
    }

    @Test
    fun `saveCredentials inserts entity with normalized email`() = runTest {
        val id = "1"
        val rawEmail = "Test@Test.com"
        val normalizedEmail = "test@test.com"
        val passwordHash = "hash"
        val expectedEntity = AuthCredentialEntity(id, normalizedEmail, passwordHash)

        coEvery { mockAuthDao.insertCredentials(expectedEntity) } returns Unit

        authRepository.saveCredentials(id, rawEmail, passwordHash)

        coVerify(exactly = 1) { mockAuthDao.insertCredentials(expectedEntity) }
    }

    @Test
    fun `getPasswordHash returns hash for normalized email`() = runTest {
        val expectedHash = "hash123"
        coEvery { mockAuthDao.getPasswordHashByEmail("test@test.com") } returns expectedHash

        val result = authRepository.getPasswordHash("  TEST@test.com  ")

        assertEquals(expectedHash, result)
        coVerify(exactly = 1) { mockAuthDao.getPasswordHashByEmail("test@test.com") }
    }

    @Test
    fun `getPasswordHash returns null when email not found`() = runTest {
        coEvery { mockAuthDao.getPasswordHashByEmail(any()) } returns null

        val result = authRepository.getPasswordHash("missing@test.com")

        assertEquals(null, result)
    }

    @Test
    fun `getUserIdByEmail returns id for normalized email`() = runTest {
        val expectedId = "user-123"
        coEvery { mockAuthDao.getUserIdByEmail("test@test.com") } returns expectedId

        val result = authRepository.getUserIdByEmail("TEST@TEST.COM")

        assertEquals(expectedId, result)
        coVerify(exactly = 1) { mockAuthDao.getUserIdByEmail("test@test.com") }
    }

    @Test
    fun `saveTokens saves both access and refresh tokens to storage`() {
        authRepository.saveTokens("access_token", "refresh_token")

        verify(exactly = 1) { mockTokenStorage.saveAccessToken("access_token") }
        verify(exactly = 1) { mockTokenStorage.saveRefreshToken("refresh_token") }
    }

    @Test
    fun `getRefreshToken delegates to TokenStorage`() {
        every { mockTokenStorage.getRefreshToken() } returns "refresh_token_123"

        val result = authRepository.getRefreshToken()

        assertEquals("refresh_token_123", result)
        verify(exactly = 1) { mockTokenStorage.getRefreshToken() }
    }

    @Test
    fun `clearSession clears all tokens from storage`() {
        authRepository.clearSession()

        verify(exactly = 1) { mockTokenStorage.clearTokens() }
    }
}
