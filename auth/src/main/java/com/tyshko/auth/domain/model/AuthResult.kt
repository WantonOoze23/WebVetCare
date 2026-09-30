package com.tyshko.auth.domain.model

sealed class AuthResult {
    data class Success(val userId: String) : AuthResult()
    sealed class Error : AuthResult(){
        object UserAlreadyExists : Error()
        object UserNotFound : Error()
        object WrongPassword : Error()
        object UserDataCorrupted : Error()
        data class Unknown(val cause: Throwable?) : Error()
    }
}