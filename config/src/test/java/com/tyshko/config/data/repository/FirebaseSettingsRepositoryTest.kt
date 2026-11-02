package com.tyshko.config.data.repository

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.ConfigUpdateListenerRegistration
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tyshko.config.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class FirebaseSettingsRepositoryTest {

    private val remoteConfig = mockk<FirebaseRemoteConfig>(relaxed = true)

    private fun repo(debug: Boolean = false) = FirebaseSettingsRepository(debug, remoteConfig)

    private fun fetchReturns(task: Task<Boolean>) {
        every { remoteConfig.fetchAndActivate() } returns task
    }


    @Test
    fun `debug true sets minimum fetch interval to 0`() {
        val slot = slot<FirebaseRemoteConfigSettings>()
        every { remoteConfig.setConfigSettingsAsync(capture(slot)) } returns Tasks.forResult(null)

        repo(debug = true)

        assertEquals(0L, slot.captured.minimumFetchIntervalInSeconds)
    }

    @Test
    fun `debug false sets minimum fetch interval to 3600`() {
        val slot = slot<FirebaseRemoteConfigSettings>()
        every { remoteConfig.setConfigSettingsAsync(capture(slot)) } returns Tasks.forResult(null)

        repo(debug = false)

        assertEquals(3600L, slot.captured.minimumFetchIntervalInSeconds)
    }

    @Test
    fun `init sets in-app defaults from xml`() {
        repo()

        verify(exactly = 1) { remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults) }
    }

    // ---- fetchAndActivate -------------------------------------------------

    @Test
    fun `fetch success with new values returns true and marks online`() = runTest {
        fetchReturns(Tasks.forResult(true))
        val repository = repo()

        assertTrue(repository.fetchAndActivate())
        assertTrue(repository.getConfig().isOnline)
    }

    @Test
    fun `fetch success without new values returns false but marks online`() = runTest {
        fetchReturns(Tasks.forResult(false))
        val repository = repo()

        assertFalse(repository.fetchAndActivate())
        assertTrue(repository.getConfig().isOnline)
    }

    @Test
    fun `throttled fetch returns false and keeps offline when never fetched`() = runTest {
        fetchReturns(Tasks.forException(FirebaseRemoteConfigFetchThrottledException(1000L)))
        val repository = repo()

        assertFalse(repository.fetchAndActivate())
        assertFalse(repository.getConfig().isOnline)
    }

    @Test
    fun `throttled fetch keeps online after earlier success`() = runTest {
        val repository = repo()
        fetchReturns(Tasks.forResult(true))
        repository.fetchAndActivate()

        fetchReturns(Tasks.forException(FirebaseRemoteConfigFetchThrottledException(1000L)))

        assertFalse(repository.fetchAndActivate())
        assertTrue(repository.getConfig().isOnline)
    }

    @Test
    fun `failed fetch returns false and marks offline`() = runTest {
        fetchReturns(Tasks.forException(IOException("no network")))
        val repository = repo()

        assertFalse(repository.fetchAndActivate())
        assertFalse(repository.getConfig().isOnline)
    }

    @Test
    fun `success followed by failure switches to offline`() = runTest {
        val repository = repo()
        fetchReturns(Tasks.forResult(true))
        repository.fetchAndActivate()
        assertTrue(repository.getConfig().isOnline)

        fetchReturns(Tasks.forException(IOException("no network")))
        repository.fetchAndActivate()

        assertFalse(repository.getConfig().isOnline)
    }

    @Test
    fun `failure followed by success switches to online`() = runTest {
        val repository = repo()
        fetchReturns(Tasks.forException(IOException("no network")))
        repository.fetchAndActivate()
        assertFalse(repository.getConfig().isOnline)

        fetchReturns(Tasks.forResult(true))
        repository.fetchAndActivate()

        assertTrue(repository.getConfig().isOnline)
    }

    @Test
    fun `cancellation is rethrown and does not change online flag`() = runTest {
        val repository = repo()
        fetchReturns(Tasks.forResult(true))
        repository.fetchAndActivate()

        every { remoteConfig.fetchAndActivate() } throws CancellationException("cancelled")

        try {
            repository.fetchAndActivate()
            throw AssertionError("CancellationException expected")
        } catch (_: CancellationException) {
        }
        assertTrue(repository.getConfig().isOnline)
    }


    @Test
    fun `getConfig maps remote config keys to model fields`() {
        every { remoteConfig.getString("test_data") } returns "data"
        every { remoteConfig.getString("app_language") } returns "uk"
        every { remoteConfig.getString("landing_particle_color") } returns "#112233"
        every { remoteConfig.getString("welcome_message") } returns "hello"

        val config = repo().getConfig()

        assertEquals("data", config.testData)
        assertEquals("uk", config.appLanguage)
        assertEquals("#112233", config.landingColor)
        assertEquals("hello", config.welcomeMessage)
    }

    @Test
    fun `getConfig is offline before first fetch`() {
        assertFalse(repo().getConfig().isOnline)
    }


    private class ListenerFixture(
        val remoteConfig: FirebaseRemoteConfig,
        activateSuccessful: Boolean
    ) {
        val registration = mockk<ConfigUpdateListenerRegistration>(relaxed = true)
        val listenerSlot = slot<ConfigUpdateListener>()

        init {
            every { remoteConfig.addOnConfigUpdateListener(capture(listenerSlot)) } returns registration

            val activateTask = mockk<Task<Boolean>>()
            every { activateTask.isSuccessful } returns activateSuccessful
            every { activateTask.addOnCompleteListener(any<OnCompleteListener<Boolean>>()) } answers {
                firstArg<OnCompleteListener<Boolean>>().onComplete(activateTask)
                activateTask
            }
            every { remoteConfig.activate() } returns activateTask
        }
    }

    @Test
    fun `real-time update with successful activate invokes callback`() {
        val fixture = ListenerFixture(remoteConfig, activateSuccessful = true)
        var calls = 0

        repo().addConfigUpdateListener { calls++ }
        fixture.listenerSlot.captured.onUpdate(mockk<ConfigUpdate>(relaxed = true))

        assertEquals(1, calls)
    }

    @Test
    fun `real-time update with failed activate does not invoke callback`() {
        val fixture = ListenerFixture(remoteConfig, activateSuccessful = false)
        var calls = 0

        repo().addConfigUpdateListener { calls++ }
        fixture.listenerSlot.captured.onUpdate(mockk<ConfigUpdate>(relaxed = true))

        assertEquals(0, calls)
    }

    @Test
    fun `listener error does not throw and does not invoke callback`() {
        val fixture = ListenerFixture(remoteConfig, activateSuccessful = true)
        var calls = 0

        repo().addConfigUpdateListener { calls++ }
        fixture.listenerSlot.captured.onError(FirebaseRemoteConfigClientException("boom"))

        assertEquals(0, calls)
    }

    @Test
    fun `closing returned handle removes registration exactly once`() {
        val fixture = ListenerFixture(remoteConfig, activateSuccessful = true)

        val handle = repo().addConfigUpdateListener { }
        handle.close()

        verify(exactly = 1) { fixture.registration.remove() }
    }
}