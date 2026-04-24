package com.tyshko.webvetcare

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tyshko.auth.di.authModule
import com.tyshko.user.di.userModule
import com.tyshko.webvetcare.ui.theme.WebVetCareTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {

            androidContext(this@MainActivity)

            modules(
                listOf(
                    authModule,
                    userModule,
                    //appModule
                )
            )
        }
    }
}