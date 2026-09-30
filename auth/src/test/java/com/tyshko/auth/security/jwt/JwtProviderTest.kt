package com.tyshko.auth.security.jwt

import com.tyshko.auth.security.crypto.RsaKeyManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

class JwtProviderTest {

    private lateinit var jwtProvider: JwtProvider
    private val mockKeyManager: RsaKeyManager = mockk()

    @Before
    fun setup() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        every { mockKeyManager.providePrivateKey() } returns keyPair.private as RSAPrivateKey
        every { mockKeyManager.providePublicKey() } returns keyPair.public as RSAPublicKey

        jwtProvider = JwtProvider(mockKeyManager)
    }

    @Test
    fun `generateAccessToken produces valid JWT with correct subject and roles`() {
        val userId = "user-123"
        val roles = listOf("User", "Patient")

        val token = jwtProvider.generateAccessToken(userId, roles)

        assertNotNull(token)
        assertTrue(token.split(".").size == 3)

        val result = jwtProvider.validateToken(token)
        assertTrue(result is TokenValidationResult.Valid)

        val decoded = (result as TokenValidationResult.Valid).decodedJWT
        assertEquals(userId, decoded.subject)
        assertEquals(roles, decoded.getClaim("roles").asList(String::class.java))
    }

    @Test
    fun `generateRefreshToken produces valid JWT with correct subject`() {
        val userId = "user-123"

        val token = jwtProvider.generateRefreshToken(userId)
        assertNotNull(token)

        val result = jwtProvider.validateToken(token)
        assertTrue(result is TokenValidationResult.Valid)

        val decoded = (result as TokenValidationResult.Valid).decodedJWT
        assertEquals(userId, decoded.subject)
    }

    @Test
    fun `validateToken returns InvalidSignature for tampered signature`() {
        val token = jwtProvider.generateAccessToken("user", emptyList())

        val parts = token.split(".")
        val tamperedToken = "${parts[0]}.${parts[1]}.invalid_sig"

        val result = jwtProvider.validateToken(tamperedToken)

        assertTrue(result is TokenValidationResult.InvalidSignature)
    }

    @Test
    fun `validateToken returns MalformedToken for completely invalid string`() {
        val result = jwtProvider.validateToken("this.is.not.a.jwt.at.all")
        assertTrue(result is TokenValidationResult.MalformedToken)
    }
}
