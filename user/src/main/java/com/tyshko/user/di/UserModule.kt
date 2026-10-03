package com.tyshko.user.di

import androidx.room.Room
import com.tyshko.user.data.local.db.UserDatabase
import com.tyshko.user.data.repository.UserRepositoryImpl
import com.tyshko.user.domain.repository.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.jvm.java

val userModule = module {

    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = UserDatabase::class.java,
            name = "user_database"
        ).build()
    }

    single { get<UserDatabase>().userDao() }

    single<UserRepository> { UserRepositoryImpl(get())}
}
