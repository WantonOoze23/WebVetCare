package com.tyshko.auth.security.crypto

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun verifyPassword(password: String, hashed: String): Boolean {
        return BCrypt.checkpw(password, hashed)
    }
}