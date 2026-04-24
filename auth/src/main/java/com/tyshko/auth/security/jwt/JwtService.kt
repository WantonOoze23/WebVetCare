package com.tyshko.auth.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.tyshko.auth.security.crypto.RsaKeyManager
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date

class JwtProvider(private val keyManager: RsaKeyManager) {

    private val issuer = "WebVetCare_Auth_Service"

    private val algorithm: Algorithm
        get() = Algorithm.RSA256(
            keyManager.providePublicKey() as RSAPublicKey,
            keyManager.providePrivateKey() as RSAPrivateKey
        )

    fun generateAccessToken(userId: String, roles: List<String>): String {
        val now = System.currentTimeMillis()
        val validity = 15 * 60 * 1000

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            .withClaim("roles", roles)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + validity))
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: String): String {
        val now = System.currentTimeMillis()
        val validity = 30L * 24 * 60 * 60 * 1000

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + validity))
            .sign(algorithm)
    }

    fun validateToken(token: String): DecodedJWT? {
        return try {
            val verifyAlgorithm = Algorithm.RSA256(keyManager.providePublicKey() as RSAPublicKey, null)
            val verifier = JWT.require(verifyAlgorithm)
                .withIssuer(issuer)
                .build()

            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }
}