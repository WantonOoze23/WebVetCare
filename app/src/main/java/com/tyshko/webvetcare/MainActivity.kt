package com.tyshko.webvetcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tyshko.auth.di.authModule
import com.tyshko.user.di.userModule
import com.tyshko.webvetcare.di.appModule
import com.tyshko.webvetcare.navigation.NavigationController
import com.tyshko.webvetcare.ui.theme.WebVetCareTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
                modules(
                    listOf(
                        authModule,
                        userModule,
                        appModule
                    )
                )
            }
        }

        setContent {
            WebVetCareTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Запуск графа навігації
                    NavigationController()
                }
            }
        }
    }
}