package com.tyshko.config.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SettingsConfigTest {

    private val base = SettingsConfig(
        appLanguage = "en",
        testData = "data",
        landingColor = "#6650A4",
        welcomeMessage = "hello",
        isOnline = true
    )

    @Test
    fun `copy changes only requested field`() {
        val copy = base.copy(isOnline = false)

        assertEquals(false, copy.isOnline)
        assertEquals(base.appLanguage, copy.appLanguage)
        assertEquals(base.testData, copy.testData)
        assertEquals(base.landingColor, copy.landingColor)
        assertEquals(base.welcomeMessage, copy.welcomeMessage)
    }

    @Test
    fun `equality is by value and sensitive to every field`() {
        assertEquals(base, base.copy())
        assertNotEquals(base, base.copy(appLanguage = "uk"))
        assertNotEquals(base, base.copy(testData = "x"))
        assertNotEquals(base, base.copy(landingColor = "#000000"))
        assertNotEquals(base, base.copy(welcomeMessage = "x"))
        assertNotEquals(base, base.copy(isOnline = false))
    }
}