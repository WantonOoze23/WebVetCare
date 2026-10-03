package com.tyshko.webvetcare.feature.auth

import com.tyshko.auth.domain.RolesProvider
import com.tyshko.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull

class UserRolesProvider (
    private val userRepository: UserRepository
) : RolesProvider{
    override suspend fun getRoles(userId: String): List<String> {
        return userRepository.getUser(userId).firstOrNull()?.roles?.map { it.name } ?: listOf("User")
    }
}