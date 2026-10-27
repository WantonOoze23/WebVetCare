package com.tyshko.config.data.repository

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.tyshko.config.R
import com.tyshko.config.domain.model.SettingsConfig
import com.tyshko.config.domain.repository.SettingsRepository
import kotlinx.coroutines.tasks.await

class FirebaseSettingsRepository(
    private val debug: Boolean = false
) : SettingsRepository {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private var fetchedSuccessfully: Boolean = false

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (debug) 0L else 3600L
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    override suspend fun fetchAndActivate(): Boolean {
        return try {
            val result = remoteConfig.fetchAndActivate().await()
            fetchedSuccessfully = true
            result
        } catch (e: FirebaseRemoteConfigFetchThrottledException) {
            Log.w("FirebaseSettingsRepo", "Fetch throttled in manual call: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("FirebaseSettingsRepo", "fetchAndActivate failed", e)
            fetchedSuccessfully = false
            false
        }
    }


    override fun getConfig(): SettingsConfig = SettingsConfig(
        testData = remoteConfig.getString("test_data"),
        appLanguage = remoteConfig.getString("app_language"),
        landingColor = remoteConfig.getString("landing_particle_color"),
        welcomeMessage = remoteConfig.getString("welcome_message"),
        isOnline = fetchedSuccessfully
    )

    override fun addConfigUpdateListener(onUpdate: () -> Unit): AutoCloseable {
        val result = remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d("FirebaseSettingsRepo", "Config updated, keys: ${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseSettingsRepo", "Config activated after real-time update")
                        onUpdate()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("FirebaseSettingsRepo", "Config update listener error", error)
            }
        })

        return AutoCloseable {
            result.remove()
        }
    }
}