package com.tyshko.auth.security.jwt

import com.auth0.jwt.interfaces.DecodedJWT

sealed class TokenValidationResult {
    data class Valid(val decodedJWT: DecodedJWT) : TokenValidationResult()
    object Expired: TokenValidationResult()
    object InvalidSignature: TokenValidationResult()
    object MalformedToken: TokenValidationResult()
}