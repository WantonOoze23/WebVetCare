package com.tyshko.auth.di

import androidx.room.Room
import com.tyshko.auth.data.local.AuthDatabase
import com.tyshko.auth.data.local.TokenStorage
import com.tyshko.auth.data.repository.AuthRepositoryImpl
import com.tyshko.auth.domain.repository.AuthRepository
import com.tyshko.auth.domain.usecase.LoginUseCase
import com.tyshko.auth.domain.usecase.RefreshTokenUseCase
import com.tyshko.auth.domain.usecase.RegisterUseCase
import com.tyshko.auth.data.local.migration.MIGRATION_1_2
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

    single<PasswordHasherContract> { PasswordHasher }

    single { TokenStorage(context = androidContext()) }

    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AuthDatabase::class.java,
            name = "auth_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    single { get<AuthDatabase>().authDao() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get(), get(), get()) }
    factory { RegisterUseCase(get(), get(), get()) }
    factory { RefreshTokenUseCase(get(), get(), get()) }
}