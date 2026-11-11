package com.tyshko.auth.security.jwt

import com.auth0.jwt.interfaces.DecodedJWT
import com.tyshko.auth.security.crypto.RsaKeyManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `generateAccessToken should return valid JWT with correct claims`() {
        val userId = "user-123"
        val roles = listOf("User", "Patient")

        val token = jwtProvider.generateAccessToken(userId, roles)
        assertNotNull(token)
        assertTrue(token.split(".").size == 3)

        val decoded: DecodedJWT? = jwtProvider.validateToken(token)
        assertNotNull(decoded)
        assertEquals(userId, decoded?.subject)
        assertEquals(roles, decoded?.getClaim("roles")?.asList(String::class.java))
    }

    @Test
    fun `generateRefreshToken should return valid JWT`() {
        val userId = "user-123"

        val token = jwtProvider.generateRefreshToken(userId)
        assertNotNull(token)

        val decoded: DecodedJWT? = jwtProvider.validateToken(token)
        assertNotNull(decoded)
        assertEquals(userId, decoded?.subject)
        assertNull(decoded?.getClaim("roles")?.asList(String::class.java))
    }

    @Test
    fun `validateToken should return null for invalid signature`() {
        val token = jwtProvider.generateAccessToken("user", emptyList())
        
        // Modify signature part
        val parts = token.split(".")
        val invalidToken = "${parts[0]}.${parts[1]}.invalid_sig"

        val decoded = jwtProvider.validateToken(invalidToken)
        assertNull(decoded)
    }
}
