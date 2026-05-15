package com.tyshko.auth.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.tyshko.auth.security.crypto.RsaKeyManager
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.Date

class JwtProvider(private val keyManager: RsaKeyManager) {

    private val issuer = "WebVetCare_Auth_Service"

    // Custom Algorithm implementation to handle Android Keystore keys
    private val algorithm: Algorithm = object : Algorithm("RS256", "SHA256withRSA") {
        override fun sign(contentBytes: ByteArray): ByteArray {
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(keyManager.providePrivateKey())
            signature.update(contentBytes)
            return signature.sign()
        }

        override fun verify(jwt: DecodedJWT) {
            val publicKey = keyManager.providePublicKey() as RSAPublicKey
            val signatureBytes = Base64.getUrlDecoder().decode(jwt.signature)
            val contentBytes = "${jwt.header}.${jwt.payload}".toByteArray()

            val verifier = Signature.getInstance("SHA256withRSA")
            verifier.initVerify(publicKey)
            verifier.update(contentBytes)

            if (!verifier.verify(signatureBytes)) {
                // 'this' now correctly refers to the anonymous Algorithm object
                throw SignatureVerificationException(this)
            }
        }
    }

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
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()

            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }
}
