package com.tyshko.auth.domain

interface RolesProvider {
    suspend fun getRoles(userId: String): List<String>
}