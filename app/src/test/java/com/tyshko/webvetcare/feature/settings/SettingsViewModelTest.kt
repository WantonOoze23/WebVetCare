package com.tyshko.webvetcare.feature.settings

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tyshko.config.domain.model.SettingsConfig
import com.tyshko.config.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = mockk<SettingsRepository>()

    private val onlineConfig = SettingsConfig(
        appLanguage = "en",
        testData = "Online_value",
        landingColor = "#f1325b",
        welcomeMessage = "Thank you",
        isOnline = true
    )
    private val offlineConfig = SettingsConfig(
        appLanguage = "en",
        testData = "offline_fallback_value",
        landingColor = "#6650A4",
        welcomeMessage = "",
        isOnline = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(repo)

    private fun TestScope.collectEffects(vm: SettingsViewModel): List<SettingsContract.Effect> {
        val effects = mutableListOf<SettingsContract.Effect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.effectFlow.toList(effects)
        }
        return effects
    }


    @Test
    fun `initial state is empty and repository is untouched`() {
        val vm = viewModel()

        assertEquals(SettingsContract.State(), vm.state.value)
        verify(exactly = 0) { repo.getConfig() }
    }

    @Test
    fun `LoadSettings copies all fields from config`() {
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()

        vm.onEvent(SettingsContract.Event.LoadSettings)

        assertEquals(
            SettingsContract.State(
                appLanguage = "en",
                testData = "Online_value",
                landingColor = "#f1325b",
                welcomeMessage = "Thank you",
                isOnline = true,
                isLoading = false
            ),
            vm.state.value
        )
    }

    @Test
    fun `LoadSettings reads config exactly once`() {
        every { repo.getConfig() } returns onlineConfig

        viewModel().onEvent(SettingsContract.Event.LoadSettings)

        verify(exactly = 1) { repo.getConfig() }
    }

    @Test
    fun `LoadSettings does not touch isLoading`() {
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()

        vm.onEvent(SettingsContract.Event.LoadSettings)

        assertFalse(vm.state.value.isLoading)
    }


    @Test
    fun `RefreshConfig with new values updates state and shows snackbar`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } returns true
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()
        val effects = collectEffects(vm)

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()

        assertEquals(onlineConfig.testData, vm.state.value.testData)
        assertTrue(vm.state.value.isOnline)
        assertFalse(vm.state.value.isLoading)
        assertEquals(
            listOf<SettingsContract.Effect>(
                SettingsContract.Effect.ShowSnackbar("Configuration updated from Firebase")
            ),
            effects
        )
    }

    @Test
    fun `RefreshConfig without new values updates state but shows no snackbar`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } returns false
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()
        val effects = collectEffects(vm)

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()

        assertEquals(onlineConfig.testData, vm.state.value.testData)
        assertFalse(vm.state.value.isLoading)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `isLoading is true while fetching and false afterwards`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } coAnswers { delay(100); true }
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        runCurrent()
        assertTrue(vm.state.value.isLoading)

        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `offline refresh shows in-app defaults and offline flag`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } returns false
        every { repo.getConfig() } returns offlineConfig
        val vm = viewModel()

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()

        assertFalse(vm.state.value.isOnline)
        assertEquals("offline_fallback_value", vm.state.value.testData)
    }

    @Test
    fun `second refresh after network returns switches offline to online`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } returnsMany listOf(false, true)
        every { repo.getConfig() } returnsMany listOf(offlineConfig, onlineConfig)
        val vm = viewModel()

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()
        assertFalse(vm.state.value.isOnline)

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()
        assertTrue(vm.state.value.isOnline)
    }


    @Test
    fun `RefreshConfig failure shows error snackbar and resets loading`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } throws RuntimeException("boom")
        val vm = viewModel()
        val effects = collectEffects(vm)

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals(
            listOf<SettingsContract.Effect>(
                SettingsContract.Effect.ShowSnackbar("Failed to fetch configuration")
            ),
            effects
        )
        assertEquals(SettingsContract.State(), vm.state.value)
    }

    @Test
    fun `isLoading is reset even when nobody collects effects`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } returns true
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `cancellation is not reported as fetch failure`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } throws CancellationException("cancelled")
        val vm = viewModel()
        val effects = collectEffects(vm)

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertFalse(vm.state.value.isLoading)
    }


    @Test
    fun `startListening registers listener and reloads values on update`() {
        val callback = slot<() -> Unit>()
        every { repo.addConfigUpdateListener(capture(callback)) } returns mockk<AutoCloseable>(relaxed = true)
        every { repo.getConfig() } returns onlineConfig
        val vm = viewModel()

        vm.startListening()
        verify(exactly = 1) { repo.addConfigUpdateListener(any()) }

        callback.captured.invoke()

        assertEquals(onlineConfig.testData, vm.state.value.testData)
        assertTrue(vm.state.value.isOnline)
    }

    @Test
    fun `startListening twice closes previous registration`() {
        val first = mockk<AutoCloseable>(relaxed = true)
        val second = mockk<AutoCloseable>(relaxed = true)
        every { repo.addConfigUpdateListener(any()) } returnsMany listOf(first, second)
        val vm = viewModel()

        vm.startListening()
        vm.startListening()

        verify(exactly = 1) { first.close() }
        verify(exactly = 0) { second.close() }
    }


    private fun viewModelWithStore(): Pair<SettingsViewModel, ViewModelStore> {
        val store = ViewModelStore()
        val factory = viewModelFactory { initializer { SettingsViewModel(repo) } }
        val vm = ViewModelProvider(store, factory).get(SettingsViewModel::class.java)
        return vm to store
    }

    @Test
    fun `onCleared closes listener registration`() {
        val registration = mockk<AutoCloseable>(relaxed = true)
        every { repo.addConfigUpdateListener(any()) } returns registration
        val (vm, store) = viewModelWithStore()

        vm.startListening()
        store.clear()

        verify(exactly = 1) { registration.close() }
    }

    @Test
    fun `onCleared without listener does not throw`() {
        val (_, store) = viewModelWithStore()

        store.clear()
    }

    @Test
    fun `onCleared cancels in-flight fetch`() = runTest(dispatcher) {
        coEvery { repo.fetchAndActivate() } coAnswers { delay(100); true }
        every { repo.getConfig() } returns onlineConfig
        val (vm, store) = viewModelWithStore()

        vm.onEvent(SettingsContract.Event.RefreshConfig)
        runCurrent()
        store.clear()
        advanceUntilIdle()

        verify(exactly = 0) { repo.getConfig() }
        assertEquals("", vm.state.value.testData)
    }
}
