package com.tyshko.auth.di

import androidx.room.Room
import com.tyshko.auth.data.local.AuthDatabase
import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.data.repository.AuthRepositoryImpl
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.domain.usecase.LoginUseCase
import com.tyshko.auth.domain.usecase.RefreshTokenUseCase
import com.tyshko.auth.domain.usecase.RegisterUseCase
import com.tyshko.auth.security.JwtProviderContract
import com.tyshko.auth.security.PasswordHasherContract
import com.tyshko.auth.security.crypto.PasswordHasher
import com.tyshko.auth.security.crypto.RsaKeyManager
import com.tyshko.auth.security.jwt.JwtProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {

    single { RsaKeyManager() }
    single<JwtProviderContract> { JwtProvider(get()) }

    single { PasswordHasher } bind PasswordHasherContract::class

    single { TokenStorage(context = androidContext()) }

    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AuthDatabase::class.java,
            name = "auth_database"
        ).build()
    }

    single { get<AuthDatabase>().authDao() }

    single { AuthRepositoryImpl(get(), get()) } bind AuthRepository::class

    factory { LoginUseCase(get(), get(), get()) }
    factory { RegisterUseCase(get(), get(), get()) }
    factory { RefreshTokenUseCase(get(), get(), get()) }
}