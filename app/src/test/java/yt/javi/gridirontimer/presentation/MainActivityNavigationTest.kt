package yt.javi.gridirontimer.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityNavigationTest {

    @Test
    fun `timer route includes flag mode`() {
        val route = MainActivity.Screen.Timer.createRoute(1_200_000L, isFlagMode = true)

        assertEquals("timer/1200000/flag", route)
    }

    @Test
    fun `timer route includes tackle mode`() {
        val route = MainActivity.Screen.Timer.createRoute(720_000L, isFlagMode = false)

        assertEquals("timer/720000/tackle", route)
    }

    @Test
    fun `parseIsFlagMode returns true only for flag`() {
        assertTrue(parseIsFlagMode("flag"))
        assertFalse(parseIsFlagMode("tackle"))
        assertFalse(parseIsFlagMode(null))
        assertFalse(parseIsFlagMode("other"))
    }
}
