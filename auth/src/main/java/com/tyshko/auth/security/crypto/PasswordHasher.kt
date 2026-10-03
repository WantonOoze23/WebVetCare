package com.tyshko.auth.security.crypto

import com.tyshko.auth.security.PasswordHasherContract
import org.mindrot.jbcrypt.BCrypt

object PasswordHasher : PasswordHasherContract{
    override fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    override fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}