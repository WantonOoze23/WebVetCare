package com.tyshko.auth.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.crypto.RsaKeyManager
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.Date

class JwtProvider(private val keyManager: RsaKeyManager) : JwtProviderContract{

    private val algorithm: Algorithm = object : Algorithm(ALGORITHM_NAME, SUGNATURE_ALGHORITM) {
        override fun sign(contentBytes: ByteArray): ByteArray {
            val signature = Signature.getInstance(SUGNATURE_ALGHORITM)
            signature.initSign(keyManager.providePrivateKey())
            signature.update(contentBytes)
            return signature.sign()
        }

        override fun verify(jwt: DecodedJWT) {
            val publicKey = keyManager.providePublicKey() as RSAPublicKey
            val signatureBytes = Base64.getUrlDecoder().decode(jwt.signature)
            val contentBytes = "${jwt.header}.${jwt.payload}".toByteArray()

            val verifier = Signature.getInstance(SUGNATURE_ALGHORITM)
            verifier.initVerify(publicKey)
            verifier.update(contentBytes)

            if (!verifier.verify(signatureBytes)) {
                throw SignatureVerificationException(this)
            }
        }
    }

    override fun generateAccessToken(userId: String, roles: List<String>): String {
        val now = System.currentTimeMillis()
        val validity = ACCESS_TOKEN_TLL_MS

        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withClaim(CLAIM_ROLES, roles)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + validity))
            .sign(algorithm)
    }

    override fun generateRefreshToken(userId: String): String {
        val now = System.currentTimeMillis()
        val validity = REFRESH_TOKEN_TTL_MS

        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + validity))
            .sign(algorithm)
    }

    override fun validateToken(token: String): TokenValidationResult {
        return try {
            val verifier = JWT
                .require(algorithm)
                .withIssuer(ISSUER)
                .build()

            TokenValidationResult.Valid(verifier.verify(token))
        } catch (e: TokenExpiredException) {
            TokenValidationResult.Expired
        } catch( e: SignatureVerificationException){
            TokenValidationResult.InvalidSignature
        } catch (e: JWTVerificationException) {
            TokenValidationResult.MalformedToken
        }
    }

    companion object{
        private val ISSUER = "WebVetCare_Auth_Service"
        private val ALGORITHM_NAME = "RS256"
        private val SUGNATURE_ALGHORITM = "SHA256withRSA"
        private val CLAIM_ROLES = "roles"
        const val ACCESS_TOKEN_TLL_MS = 15L * 60 * 1000L
        const val REFRESH_TOKEN_TTL_MS = 30L * 24 * 60 * 60 * 1000L
    }
}
