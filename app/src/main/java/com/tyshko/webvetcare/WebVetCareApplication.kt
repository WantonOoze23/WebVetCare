package com.tyshko.webvetcare

import android.app.Application
import com.tyshko.auth.di.authModule
import com.tyshko.user.di.userModule
import com.tyshko.webvetcare.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WebVetCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WebVetCareApplication)
            modules(
                listOf(
                    authModule,
                    userModule,
                    appModule
                )
            )
        }
    }
}