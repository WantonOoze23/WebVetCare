package com.tyshko.webvetcare.firebase.data.repository

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.tyshko.webvetcare.R
import com.tyshko.webvetcare.firebase.domain.repository.SettingsRepository
import kotlinx.coroutines.tasks.await

class FirebaseSettingsRepository : SettingsRepository {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseSettingsRepo", "Remote config fetched and activated")
            } else {
                val exception = task.exception
                if (exception is FirebaseRemoteConfigFetchThrottledException) {
                    Log.w("FirebaseSettingsRepo", "Fetch throttled: ${exception.message}")
                } else {
                    Log.w("FirebaseSettingsRepo", "Failed to fetch remote config", exception)
                }
            }
        }
    }

    override fun getTestData(): String {
        return remoteConfig.getString("test_data")
    }

    override fun getLandingColor(): String {
        return remoteConfig.getString("landing_particle_color")
    }

    override fun getWelcomeMessage(): String {
        return remoteConfig.getString("welcome_message")
    }

    override fun isOnline(): Boolean {
        return remoteConfig.info.lastFetchStatus == FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS
    }

    override suspend fun fetchAndActivate(): Boolean {
        return try {
            val result = remoteConfig.fetchAndActivate().await()
            Log.d("FirebaseSettingsRepo", "fetchAndActivate result: $result")
            result
        } catch (e: FirebaseRemoteConfigFetchThrottledException) {
            Log.w("FirebaseSettingsRepo", "Fetch throttled in manual call: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("FirebaseSettingsRepo", "fetchAndActivate failed", e)
            false
        }
    }

    override fun getConfigUpdateListener(onUpdate: () -> Unit) {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
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
    }
}