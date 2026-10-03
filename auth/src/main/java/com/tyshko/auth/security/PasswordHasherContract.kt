package com.tyshko.auth.security

interface PasswordHasherContract {
    fun hashPassword(password: String): String
    fun verifyPassword(password: String, hashedPassword: String): Boolean
}