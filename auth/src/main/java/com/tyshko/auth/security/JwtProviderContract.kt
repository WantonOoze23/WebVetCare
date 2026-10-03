package com.tyshko.auth.security

import com.tyshko.auth.security.jwt.TokenValidationResult

interface JwtProviderContract {
    fun generateAccessToken(userId: String, roles: List<String>): String
    fun generateRefreshToken(userId: String): String
    fun validateToken(token: String): TokenValidationResult
}